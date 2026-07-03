# GamerCommunity

다양한 플랫폼(PlayStation, Xbox, Nintendo, PC, Mobile)의 게이머들이 게임별 게시판에서 공략, 리뷰, 질문을 나눌 수 있는 게임 커뮤니티입니다.

실사용자를 가정하고  **조회수 락 경합, 반정규화 컬럼 정합성, LLM 비동기 처리 등 동시성 문제를 직접 계측하고 해결**하는 데 집중한 프로젝트입니다.

> **Live**: [http://100.51.108.228](http://100.51.108.228/) (배포 서버 - 현재 운영 x)

> **Blog**: [dogtae.tistory.com](https://dogtae.tistory.com/)
---


## Demo

**게시글 작성 / 조회 / 수정 / 삭제**

![CRUD Demo](docs/crud-demo.gif)

**AI 답변 생성 (요청 즉시 응답 후 비동기 폴링으로 결과 표시)**

![AI Demo](docs/ai-demo.gif)

---

## Architecture

![Architecture](docs/architecture.svg)

---

## Tech Stack

| 구분 | 기술 |
| --- | --- |
| **Backend** | Spring Boot 3.2.5, Java 17, Gradle |
| **Database** | MySQL 8.0, JPA, QueryDSL 5.0 |
| **Cache** | Redis (Lettuce) |
| **Auth** | Spring Security, JWT (jjwt 0.12) |
| **AI** | OpenAI API (gpt-4o-mini), Spring @Async |
| **Batch / Scheduler** | Spring Batch, Spring Scheduler |
| **Crawling** | ROME (RSS), Jsoup |
| **Storage** | AWS S3 |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Zustand |
| **Infra** | AWS EC2 (t4g.small), Docker, Docker Compose |
| **CI/CD** | GitHub Actions, Docker Hub |
| **Monitoring** | Prometheus, Grafana, Actuator |
| **Load Test** | k6 |

### Server Environment

| 서버 | 사양 | 역할 |
| --- | --- | --- |
| App Server | AWS EC2 t4g.small (2 vCPU) | Spring Boot 애플리케이션 |
| DB Server | AWS EC2 t4g.small (2 vCPU) | MySQL 8.0 (HikariCP max 30) |

---

## Key Features

### 게시판

게임별 게시판에서 게시글을 작성하고, 태그(공략/일반/질문/정보)와 정렬(최신/조회수/추천/댓글수) 조건으로 필터링할 수 있습니다. QueryDSL 동적 쿼리를 사용하여 조건 조합에 따라 쿼리를 생성합니다.

### 조회수 처리 (인메모리 Write-Back)

조회수를 `ConcurrentHashMap`에 인메모리로 누적한 뒤, 스케줄러가 주기적으로 JDBC `batchUpdate`를 통해 DB에 일괄 반영합니다. `ViewCount` 인터페이스로 DB 직접 / 인메모리 / Redis 방식을 전환할 수 있도록 구성했습니다. (개선 과정은 [Troubleshooting](#1-조회수-update-락-경합--응답시간-592ms에서-11ms로-54배-개선) 참고)

### 리뷰 평점 (집계 컬럼)

리뷰 등록, 삭제, 수정 시 `ratingSum`과 `reviewCount` 집계 컬럼을 단일 `@Modifying` UPDATE로 원자적으로 갱신합니다. `SELECT AVG` 없이 평점을 계산하며, 낙관적 락과 비관적 락 전략도 비교용으로 함께 구현했습니다.

### AI 답변기능 (비동기 폴링)

게시글 작성자가 AI 답변을 요청하면 서버는 즉시 taskId를 반환하고, LLM 호출과 댓글 저장은 별도 비동기 스레드풀(llmExecutor)에서 처리합니다. 프론트엔드는 1초 간격 폴링으로 생성 상태(PENDING → COMPLETED / FAILED)를 확인하여 결과를 표시합니다. DB 원자적 UPDATE 기반 락으로 동시 요청을 제어하고 LLM 호출 성공 후에만 횟수를 차감합니다. 유저당 하루 3회 사용 제한.

### 실시간 인기글

댓글(+3), 추천(+5), 조회(체크포인트) 이벤트에 가중치를 부여하여 게시글 점수를 산출합니다. 점수를 원자적으로 증감하고, 5분 주기 스케줄러가 인기글 플래그를 갱신합니다. 조회 점수는 매 요청마다 반영하지 않고, 조회수가 100 단위를 넘을 때만 체크포인트 방식으로 반영하여 락 경합을 방지했습니다.

### 게임 뉴스 수집

ROME(RSS)과 Jsoup 기반 크롤러가 매일 오전 6시에 게임메카, 루리웹 뉴스를 자동 수집합니다. 스케줄러가 수집을 트리거하고, 소스별 크롤러를 분리하여 구현했습니다.

### 인기 게시판 통계

게시판별 활동량(게시글, 댓글, 추천)을 일간/주간/월간 단위로 집계하는 배치를 운영합니다. 기간별 통계 엔티티를 분리하여 각각의 스케줄러가 독립적으로 실행됩니다.

---

## Troubleshooting

문제를 추측이 아닌 k6 부하 테스트로 계측하고, 복수의 해결안을 비교한 뒤 트레이드오프를 근거로 선택했습니다. 상세 과정은 각 블로그 글에 기록되어 있습니다.

> 테스트 환경: AWS EC2 t4g.small (2 vCPU) 2대 (App / MySQL 분리), HikariCP max 30, k6

### 1. 조회수 UPDATE 락 경합 — 응답시간 592ms에서 11ms로 (54배 개선)

**문제** — 매 조회마다 실행되는 `UPDATE post SET views = views + 1`로 전체 TPS 37% 하락, DB CPU 89% 점유. 인기글에 트래픽 집중 시(200 req/s) 로우 락 26~29건이 대기하며 평균 응답시간이 16ms에서 592ms로 악화.

**해결** — 조회수를 인메모리(`ConcurrentHashMap` + `LongAdder`)에 누적하고 3초 주기 JDBC 배치로 DB에 반영하는 Write-Back 구조로 전환. Redis Cache-Aside 방식과 비교 검증 후, SPOF 제거와 운영 단순성을 근거로 인메모리 방식을 채택. `@PreDestroy` graceful shutdown과 실패 청크 재병합으로 데이터 유실을 최대 3초 이내로 제한.

| 지표 | 개선 전 | 개선 후 |
| --- | --- | --- |
| 전체 TPS | 892 | **1,402 (57% 개선)** |
| 인기글 집중 트래픽 평균 응답시간 (200 req/s) | 592ms | **11ms (54배 개선)** |

> [Redis 없이 서버 인메모리 Write-Back으로 조회수 락 경합 해결](https://dogtae.tistory.com/2)

### 2. 반정규화 컬럼 동시성 — 배치, 명시적 락 없이 정합성 확보

**문제** — `likeCount`, `ratingSum` 등 반정규화 컬럼을 JPA로 SELECT 후 UPDATE 하는 방식은 동시 요청 시 갱신 유실(lost update)이 발생.

**해결** — 낙관적 락, 비관적 락 전략을 각각 구현해 비교한 뒤, 단일 `@Modifying` 원자적 UPDATE로 DB가 직접 증감을 처리하도록 전환. 락 대기와 재시도 로직 없이 정합성을 확보하고 `SELECT AVG` 쿼리도 제거.

> [배치, 명시적 락 없이 반정규화 컬럼 정합성 문제 해결](https://dogtae.tistory.com/3)

### 3. LLM 외부 API 호출 — 커넥션 점유, 쿼터 정합성 문제

**문제** — LLM 응답 대기(수 초) 동안 톰캣 스레드와 DB 커넥션이 점유되어 처리량 저하. 동시 요청 시 일일 사용량 차감이 중복 처리되는 정합성 문제.

**해결** — 요청 즉시 taskId를 반환하고 LLM 호출은 별도 스레드풀(`llmExecutor`)에서 비동기 처리, 트랜잭션 경계를 Facade → Service → Writer로 분리해 외부 API 호출이 DB 커넥션을 잡지 않도록 설계. DB 원자적 UPDATE 기반 락으로 중복 요청을 차단하고 호출 성공 후에만 쿼터 차감.

> [LLM 외부 API 호출 시 발생한 커넥션, 정합성, 스레드풀 문제 단계적 해결](https://dogtae.tistory.com/5)

---

## Optimization

- 단건 SELECT API에서 `@Transactional(readOnly)` 제거로 불필요한 트랜잭션 오버헤드 제거 (목록 조회, 상세 조회 등 적용)
- FK 제약조건 제거(`ConstraintMode.NO_CONSTRAINT`)로 데드락 방지, 애플리케이션 레벨에서 참조 무결성 보장
- Soft Delete로 삭제 데이터 보존 및 복구 가능성 확보
- JPA SELECT 후 UPDATE 방식 대신 `@Modifying` 원자적 UPDATE로 동시성 문제 방지 (`likeCount`, `commentCount`, `postCount`, `reviewCount`, `popularScore` 등 전체 반정규화 컬럼 적용)
- Fetch Join으로 게시글 상세 조회 시 N+1 문제 방지 (`author`, `category` 즉시 로딩)
- 반정규화 컬럼(`views`, `likeCount`, `commentCount`, `rating`, `ratingSum`)으로 조회 시 JOIN, COUNT 쿼리 제거
- JDBC `batchUpdate`로 인메모리 조회수를 한 번의 쿼리로 일괄 반영

---

## CI/CD

main 브랜치에 push하면 GitHub Actions가 백엔드와 프론트엔드 Docker 이미지를 빌드하여 Docker Hub에 푸시하고, EC2에서 이미지를 pull 받아 자동으로 재배포합니다.

```
push to main → GitHub Actions (Gradle 빌드) → Docker 이미지 빌드 / Docker Hub 푸시 → EC2 pull, 컨테이너 재기동
```

---

## Project Structure

```
src/main/java/com/gamercommunity/
├── ai/                    # AI 댓글 (Facade → Service → Writer)
│   ├── facade/            # 쿼터 체크 → 락 → 비동기 위임
│   ├── service/           # 비동기 LLM 호출
│   └── usage/             # 일일 사용량 관리
├── auth/                  # JWT 발급, 재발급
├── aws/s3/                # S3 이미지 업로드
├── category/              # 게임 게시판 (계층 구조, 장르)
├── comment/               # 댓글
├── commentLike/           # 댓글 추천
├── genre/                 # 게임 장르
├── global/
│   ├── config/            # Security, Redis, QueryDSL, Async
│   ├── exception/         # 커스텀 예외 + GlobalExceptionHandler
│   └── time/              # BaseEntity (createdAt, updatedAt)
├── news/                  # 게임 뉴스 크롤링 (RSS/Jsoup) + 스케줄러
├── popular/               # 실시간 인기글 점수
├── post/                  # 게시글
│   └── view/              # 조회수 전략 (DB / InMemory / Redis)
├── postLike/              # 게시글 추천
├── review/                # 리뷰 (낙관적 락 / 비관적 락 / 집계 컬럼)
├── reviewLike/            # 리뷰 추천
├── security/jwt/          # JwtTokenProvider, JwtAuthenticationFilter
├── stats/                 # 인기 게시판 통계 (일간/주간/월간)
└── user/                  # 회원 (등급 LEVEL1~3)
```
