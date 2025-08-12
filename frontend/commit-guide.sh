#!/bin/bash
# ============================================================
#  gamers-community-frontend 커밋 가이드 (상세)
#  프로젝트 루트에서 순서대로 실행
# ============================================================

git init

# ==========================================
#  프로젝트 초기 세팅
# ==========================================

git add .gitignore
git commit -m "chore: .gitignore 설정

- node_modules, dist, .env 등 제외 규칙"

git add package.json package-lock.json
git commit -m "init: package.json 의존성 정의
git remote add origin https://github.com/kimkitae510/gamer-community.git
- react 18, react-router-dom, axios, zustand
- vite, typescript, tailwindcss, eslint
- react-select (카테고리 선택 UI)"

git add vite.config.js tsconfig.json tsconfig.app.json tsconfig.node.json
git commit -m "init: Vite + TypeScript 빌드 설정

- vite.config.js: dev 서버 설정, React 플러그인
- tsconfig.json: 컴파일러 옵션 (strict, jsx, paths)"

git add eslint.config.js
git commit -m "init: ESLint 설정

- React + TypeScript 린트 규칙"

git add tailwind.config.js postcss.config.js postcss.config.cjs
git commit -m "style: Tailwind CSS + PostCSS 설정

- tailwind.config.js: 커스텀 컬러(primary, neutral), 폰트 설정
- postcss.config: Tailwind, Autoprefixer 플러그인"

git add index.html
git commit -m "init: index.html 엔트리 HTML

- Vite 앱 루트 (#root), main.tsx 스크립트 로드"

git add src/index.css src/styles/globals.css src/styles/components.css
git commit -m "style: 글로벌 CSS + 컴포넌트 스타일

- globals.css: Tailwind base/components/utilities 레이어
- components.css: 커스텀 유틸리티 클래스 (container-custom 등)"

git add src/vite-env.d.ts
git commit -m "init: Vite 환경 타입 선언 (vite-env.d.ts)"

# ==========================================
#  앱 엔트리포인트
# ==========================================

git add src/main.tsx
git commit -m "feat: main.tsx 앱 엔트리포인트

- ReactDOM.createRoot로 #root에 App 마운트
- 글로벌 CSS import"

git add src/App.tsx
git commit -m "feat: App.tsx 루트 컴포넌트

- Router 컴포넌트 렌더링"

# ==========================================
#  API 기반 레이어
# ==========================================

git add src/api/axios.ts
git commit -m "feat: axios 인스턴스 설정

- baseURL: localhost:8081/api
- 요청 인터셉터: Authorization 헤더에 Access Token 자동 주입
- 응답 인터셉터: 401 시 /auth/reissue로 토큰 재발급 후 요청 재시도"

git add src/api/types/auth.types.ts
git commit -m "feat: 인증 타입 정의 (auth.types.ts)

- LoginRequest, JoinRequest: 로그인/회원가입 요청 DTO
- TokenResponse, TokenRequest: JWT 토큰 DTO
- UserInfo: 사용자 정보 (loginId, username, role, grade)"

git add src/api/types/post.types.ts
git commit -m "feat: 게시글 타입 정의 (post.types.ts)

- Post: 게시글 엔티티 (id, title, content, author, views, likeCount 등)
- PostTag: 태그 enum (일반, 질문, 정보, 공략)
- PostSort: 정렬 enum (latest, views, likes)
- NewsItem, Platform: 뉴스 관련 타입"

git add src/api/types/comment.types.ts
git commit -m "feat: 댓글 타입 정의 (comment.types.ts)

- Comment: 댓글 엔티티 (id, content, author, parentId, children 등)"

git add src/api/types/category.types.ts
git commit -m "feat: 카테고리 타입 정의 (category.types.ts)

- Category: 카테고리 엔티티 (id, name, imageUrl, rating 등)
- TopCategory: 인기 게시판 (rank, postCount, rating)"

git add src/api/types/review.types.ts
git commit -m "feat: 리뷰 타입 정의 (review.types.ts)

- Review: 리뷰 엔티티 (id, content, rating, author 등)"

git add src/api/types/like.types.ts
git commit -m "feat: 좋아요 타입 정의 (like.types.ts)

- LikeStatus: 좋아요 상태 응답 타입"

git add src/api/types/trending.types.ts
git commit -m "feat: 트렌딩 타입 정의 (trending.types.ts)

- TrendingPost: 인기글 (postId, title, views, likeCount, score 등)"

git add src/api/types/index.ts
git commit -m "feat: API 타입 barrel export (types/index.ts)

- 모든 타입 파일을 한 곳에서 re-export"

# ==========================================
#  인증 시스템
# ==========================================

git add src/store/auth.ts
git commit -m "feat: Zustand 인증 스토어 (auth.ts)

- AuthState: accessToken, refreshToken, user 상태 관리
- setTokens(): 토큰 저장
- setUser(): 사용자 정보 저장
- logout(): 상태 초기화 + localStorage 삭제
- persist 미들웨어로 새로고침 시에도 로그인 유지"

git add src/api/services/authService.ts
git commit -m "feat: authService - 인증 API 서비스

- reissue(): Access Token 재발급 (POST /auth/reissue)
- logout(): 로그아웃 (POST /auth/logout)
- getMe(): 현재 사용자 정보 조회 (GET /auth/me)"

git add src/api/services/userService.ts
git commit -m "feat: userService - 유저 API 서비스

- checkLoginId(): 아이디 중복 체크 (GET /users/check-id)
- checkNickname(): 닉네임 중복 체크 (GET /users/check-nickname)
- login(): 로그인 (POST /users/login) → TokenResponse
- join(): 회원가입 (POST /users/join)"

git add src/pages/auth/LoginPage.tsx
git commit -m "feat: LoginPage - 로그인 페이지

- loginId, password 입력 폼
- userService.login() → 토큰 저장 → authService.getMe() → 유저 정보 저장
- 로그인 전 페이지로 리다이렉트 (location.state.from)
- Enter 키 로그인 지원"

git add src/pages/auth/JoinPage.tsx
git commit -m "feat: JoinPage - 회원가입 페이지

- loginId, password, passwordCheck, nickname 입력 폼
- 아이디/닉네임 실시간 중복 체크 (checkLoginIdDuplicate, checkNicknameDuplicate)
- 비밀번호 일치 검증
- userService.join() → 완료 시 로그인 페이지로 이동"

# ==========================================
#  공통 UI 컴포넌트
# ==========================================

git add src/components/ui/Button.tsx
git commit -m "feat: Button 공통 컴포넌트

- variant: primary | secondary | outline | ghost
- size: sm | md | lg
- fullWidth 옵션, disabled 스타일
- HTMLButtonElement 속성 상속 (ButtonProps extends ButtonHTMLAttributes)"

git add src/components/ui/Card.tsx
git commit -m "feat: Card 공통 컴포넌트

- Card: 카드 래퍼 (hover 효과, 패딩, 그림자)
- CardHeader: 카드 상단 영역
- CardBody: 카드 본문 영역
- CardFooter: 카드 하단 영역"

git add src/components/ui/Badge.tsx
git commit -m "feat: Badge 공통 컴포넌트

- variant: default | primary | success | warning | danger
- size: sm | md
- 태그/라벨 표시용 인라인 뱃지"

git add src/components/ui/Input.tsx
git commit -m "feat: Input 공통 컴포넌트

- label, error 메시지 표시
- HTMLInputElement 속성 상속
- 에러 상태 시 빨간 테두리 스타일"

git add src/components/ui/index.ts
git commit -m "feat: UI 컴포넌트 barrel export (ui/index.ts)

- Button, Card, CardHeader, CardBody, CardFooter, Badge, Input re-export"

# ==========================================
#  레이아웃 + 헤더 + 라우터
# ==========================================

git add src/components/Layout.tsx
git commit -m "feat: Layout 래퍼 컴포넌트

- 좌측 사이드바 (카테고리 목록)
- 메인 컨텐츠 영역 (children)"

git add src/components/Header.tsx
git commit -m "feat: Header 네비게이션 컴포넌트

- 로고 + CategoryTabs (상위 카테고리 탭)
- 로그인 상태에 따라 로그인/로그아웃 버튼 토글
- 게시판 만들기 버튼 (LEVEL3 이상)
- logout(): authService.logout() → 스토어 초기화 → 홈 이동"

git add src/router.tsx
git commit -m "feat: React Router 라우트 설정

- /: HomePage (메인)
- /login, /join: 로그인/회원가입
- /trending: 실시간 인기글
- /board/create: 게시판 생성
- /:categoryName: 카테고리 게시판 목록
- /:categoryName/game/:subCategoryId: 게시글 목록
- /:categoryName/game/:id/posts/write|edit|view: 게시글 CRUD
- /:categoryName/game/:gameId/reviews: 리뷰 페이지"

# ==========================================
#  게시글 API 서비스
# ==========================================

git add src/api/services/postService.ts
git commit -m "feat: postService - 게시글 API 서비스

- getList(): 게시글 목록 조회 (페이지네이션, 태그, 정렬, 검색)
- getById(): 게시글 상세 조회
- create(): 게시글 작성
- update(): 게시글 수정
- delete(): 게시글 삭제
- getLatest(): 최신 게시글 조회 (홈페이지용)"

git add src/api/services/newsService.ts
git commit -m "feat: newsService - 뉴스 API 서비스

- getLatest(): 최신 뉴스 조회 (GET /news/latest)"

git add src/api/services/likeService.ts
git commit -m "feat: likeService - 좋아요 API 서비스

- togglePostLike(): 게시글 좋아요 토글
- toggleCommentLike(): 댓글 좋아요 토글
- toggleReviewLike(): 리뷰 좋아요 토글
- getPostLikeStatus(): 게시글 좋아요 상태 조회
- getCommentLikeStatuses(): 댓글 좋아요 상태 일괄 조회"

# ==========================================
#  홈페이지
# ==========================================

git add src/pages/posts/HomePage.tsx
git commit -m "feat: HomePage - 메인 홈페이지

- 게임 배너 이미지
- 최신 게시글 목록 (postService.getLatest)
- 최신 뉴스 카드 (newsService.getLatest)
- 실시간 인기글 사이드바 (trendingService.getTop10Trending)
- 인기 게시판 사이드바 (topCategoryService, 일간/주간/월간)
- formatPostDate, formatNewsDate로 상대 시간 표시"

# ==========================================
#  게시글 목록 페이지
# ==========================================

git add src/pages/posts/PostListPage.tsx
git commit -m "feat: PostListPage - 게시글 목록 페이지

- 카테고리별 게시글 목록 (postService.getList)
- 태그 필터링 (일반, 질문, 정보, 공략)
- 정렬 (최신순, 조회순, 추천순)
- 키워드 검색
- 페이지네이션 (URL 쿼리 파라미터 연동)
- 사이드바: 실시간 인기글, 인기 게시판"

# ==========================================
#  게시글 상세 페이지
# ==========================================

git add src/pages/posts/PostDetailPage.tsx
git commit -m "feat: PostDetailPage - 게시글 상세 페이지

- 게시글 상세 조회 (postService.getById) + 조회수 증가
- 좋아요 토글 (likeService.togglePostLike)
- 수정/삭제 버튼 (작성자 본인만 노출)
- AI 답변 기능 (aiService.askAi) - 질문 태그 게시글 전용
- 수정 여부 표시 (isEdited)
- CommentSection 포함
- 사이드바: 실시간 인기글, 인기 게시판"

# ==========================================
#  게시글 작성 페이지
# ==========================================

git add src/pages/posts/PostWritePage.tsx
git commit -m "feat: PostWritePage - 게시글 작성 페이지

- 카테고리 writable 체크 (categoryService.getChildById)
- 태그 선택 (일반, 질문, 정보, 공략)
- 제목, 본문 입력 폼
- postService.create()로 게시글 생성
- 로그인 안 된 상태면 로그인 페이지로 리다이렉트"

# ==========================================
#  게시글 수정 페이지
# ==========================================

git add src/pages/posts/PostEditPage.tsx
git commit -m "feat: PostEditPage - 게시글 수정 페이지

- 기존 게시글 데이터 로드 (postService.getById)
- 제목, 본문, 태그 수정 폼
- 변경 감지 (originalTitle, originalContent, originalTag 비교)
- postService.update()로 게시글 수정"

# ==========================================
#  댓글 시스템
# ==========================================

git add src/api/services/commentService.ts
git commit -m "feat: commentService - 댓글 API 서비스

- getByPostId(): 게시글별 댓글 목록 조회
- create(): 댓글 작성
- update(): 댓글 수정
- delete(): 댓글 삭제
- reply(): 대댓글 작성"

git add src/pages/comments/CommentSection.tsx
git commit -m "feat: CommentSection - 댓글 컴포넌트

- 댓글 목록 조회 (commentService.getByPostId)
- 댓글 작성, 수정, 삭제
- 대댓글(답글) 기능 (parentId 기반 트리 구조)
- 댓글 좋아요 (likeService.toggleCommentLike)
- AI 봇 댓글 구분 (AI_BOT_ID: rlarlxo51000)
- 작성자 뱃지 표시 (postAuthorName 비교)
- formatSmartDate로 상대 시간 표시"

# ==========================================
#  리뷰 시스템
# ==========================================

git add src/api/services/reviewService.ts
git commit -m "feat: reviewService - 리뷰 API 서비스

- getByGameId(): 게임별 리뷰 목록 조회
- create(): 리뷰 작성 (별점 + 내용)
- update(): 리뷰 수정
- delete(): 리뷰 삭제"

git add src/pages/posts/ReviewPage.tsx
git commit -m "feat: ReviewPage - 게임 리뷰 페이지

- 게임 정보 표시 (이미지, 이름, 평균 평점, 리뷰 수)
- 별점 선택 UI (1~5점, 호버 효과)
- 리뷰 작성, 수정, 삭제
- 리뷰 좋아요 (likeService.toggleReviewLike)
- 본인 리뷰만 수정/삭제 버튼 노출
- 정렬: 최신순 / 추천순"

# ==========================================
#  카테고리(게시판) 시스템
# ==========================================

git add src/api/services/categoryService.ts
git commit -m "feat: categoryService - 카테고리 API 서비스

- getParents(): 상위 카테고리 목록
- getChildren(): 하위 카테고리(게임) 목록
- getChildById(): 하위 카테고리 상세
- createChild(): 게시판 생성
- deleteChild(): 게시판 삭제
- uploadChildImage(): 게시판 이미지 업로드
- updateName(): 게시판 이름 수정
- updateGenres(): 게시판 장르 수정
- getNewCategories(): 신규 게시판 목록"

git add src/category/types.ts
git commit -m "feat: 카테고리 공통 타입 정의 (types.ts)

- Genre: 장르 (id, name)
- Category: 카테고리 (id, name, imageUrl, rating, genres, postCount 등)"

git add src/category/selectStyles.ts
git commit -m "feat: react-select 커스텀 스타일 (selectStyles.ts)

- 장르 선택 드롭다운 스타일 정의"

git add src/category/useCategoryData.ts
git commit -m "feat: useCategoryData 커스텀 훅

- 상위 카테고리 + 하위 카테고리(게임) 데이터 로드
- 장르 필터링 로직
- 검색 로직
- URL 파라미터 연동"

git add src/category/CategoryTabs.tsx
git commit -m "feat: CategoryTabs - 상위 카테고리 탭 네비게이션

- categoryService.getParents()로 플랫폼 목록 조회
- 현재 URL path 기반 활성 탭 하이라이트
- Header에 포함되어 전역 네비게이션 역할"

git add src/category/CategoryHeader.tsx
git commit -m "feat: CategoryHeader - 카테고리 페이지 헤더

- 카테고리 이름, 설명 표시
- 게시판 수, 게시글 수 통계"

git add src/category/GameCard.tsx
git commit -m "feat: GameCard - 게임 카드 UI 컴포넌트

- 게임 썸네일 이미지
- 게임 이름, 평점, 게시글 수 표시
- 장르 뱃지
- 클릭 시 해당 게임 게시판으로 이동"

git add src/category/GameCardGrid.tsx
git commit -m "feat: GameCardGrid - 게임 카드 그리드 레이아웃

- GameCard를 반응형 그리드로 배치
- 빈 목록 시 안내 메시지"

git add src/category/GenreSidebar.tsx
git commit -m "feat: GenreSidebar - 장르 필터 사이드바

- 전체 장르 목록 표시
- 선택된 장르 하이라이트
- 클릭 시 해당 장르 게임만 필터링"

git add src/category/PopularBoardsSidebar.tsx
git commit -m "feat: PopularBoardsSidebar - 인기 게시판 사이드바 (placeholder)"

git add src/category/CategoryBoardPage.tsx
git commit -m "feat: CategoryBoardPage - 카테고리 게시판 메인 페이지

- 상위 카테고리별 게임 게시판 목록 표시
- GenreSidebar로 장르 필터링
- GameCardGrid로 게임 카드 그리드 렌더링
- 게시판 관리 기능 (LEVEL3 이상: 이미지 업로드, 이름/장르 수정, 삭제)
- 검색 기능
- 인기 게시판 사이드바, 신규 게시판 섹션
- useCategoryData 훅으로 데이터 관리"

git add src/category/CategoryCreatePage.tsx
git commit -m "feat: CategoryCreatePage - 게시판 생성 페이지

- 상위 카테고리 선택 (categoryService.getParents)
- 게시판 이름 입력
- 장르 다중 선택 (체크박스)
- categoryService.createChild()로 게시판 생성"

# ==========================================
#  트렌딩 시스템
# ==========================================

git add src/api/services/trendingService.ts
git commit -m "feat: trendingService - 트렌딩 API 서비스

- getTop10Trending(): 실시간 인기글 TOP 10 (사이드바용)
- getAllTrending(): 전체 인기글 목록 (트렌딩 페이지용)"

git add src/api/services/topCategoryService.ts
git commit -m "feat: topCategoryService - 인기 게시판 API 서비스

- getDailyTop(): 일간 인기 게시판
- getWeeklyTop(): 주간 인기 게시판
- getMonthlyTop(): 월간 인기 게시판"

git add src/pages/trending/TrendingPage.tsx
git commit -m "feat: TrendingPage - 실시간 인기글 전체 목록 페이지

- trendingService.getAllTrending()으로 전체 인기글 조회
- 제목, 카테고리, 조회수, 추천수, 댓글수 표시
- 24시간 이내 100점 이상 달성 게시글 대상"

# ==========================================
#  AI 서비스
# ==========================================

git add src/api/services/aiService.ts
git commit -m "feat: aiService - AI 답변 API 서비스

- askAi(): AI에게 게시글 답변 요청 (POST /ai/ask/:postId)"

# ==========================================
#  서비스 barrel export
# ==========================================

git add src/api/services/index.ts
git commit -m "feat: API 서비스 barrel export (services/index.ts)

- authService, userService, postService, commentService
- categoryService, reviewService, likeService, newsService
- trendingService, topCategoryService, aiService 통합 export"

# ==========================================
#  리팩토링
# ==========================================

git add src/utils/dateFormat.ts
git commit -m "refactor: 날짜 포맷 유틸 함수 통합 (dateFormat.ts)

- formatSmartDate(): 스마트 상대 시간 (방금 전, N분 전, N시간 전)
- formatPostDate(): 게시글 목록용 날짜
- formatDateTime(): 상세 날짜/시각 (YYYY.MM.DD HH:MM:SS)
- formatNewsDate(): 뉴스용 날짜 (오늘, 어제, N일 전)
- isEdited(): 게시글 수정 여부 체크
- 6개 파일에서 중복 정의되던 날짜 함수 통합"

git add src/hooks/useSidebarData.ts
git commit -m "refactor: useSidebarData 커스텀 훅

- trendingPosts: 실시간 인기글 TOP 10
- topCategories: 인기 게시판 목록
- selectedPeriod: 일간/주간/월간 선택 상태
- handlePeriodChange(): 기간 변경 핸들러
- PostListPage, PostDetailPage, CategoryBoardPage 중복 로직 통합"

git add src/components/sidebar/TrendingPostsSidebar.tsx
git commit -m "refactor: TrendingPostsSidebar 공통 컴포넌트

- 실시간 인기글 TOP 10 사이드바
- 게시글 제목, 댓글 수, 카테고리 표시
- 클릭 시 해당 게시글로 이동
- 3개 페이지에서 중복되던 사이드바 JSX 통합"

git add src/components/sidebar/TopCategoriesSidebar.tsx
git commit -m "refactor: TopCategoriesSidebar 공통 컴포넌트

- 인기 게시판 TOP 7 사이드바
- 일간/주간/월간 탭 전환
- 1위: 큰 카드 (이미지 + 평점 + 게시글 수)
- 2~7위: 리스트 형태
- 3개 페이지에서 중복되던 사이드바 JSX 통합"

# ==========================================
#  정적 에셋 + 문서
# ==========================================

git add src/assets/
git commit -m "chore: 정적 에셋 추가

- game.JPG: 홈페이지 배너 이미지"

git add public/
git commit -m "chore: public 정적 파일"

git add README.md UI_MIGRATION_GUIDE.md
git commit -m "docs: README 및 UI 마이그레이션 가이드"

# ==========================================
#  마무리
# ==========================================
echo ""
echo "=== 커밋 완료! ==="
echo ""
echo "빠진 파일 확인:"
git status
echo ""
echo "빠진 거 있으면:"
echo "  git add <파일>"
echo "  git commit -m 'chore: 누락 파일 추가'"
echo ""
echo "원격 저장소 연결:"
echo "  git remote add origin <URL>"
echo "  git branch -M main"
echo "  git push -u origin main"
