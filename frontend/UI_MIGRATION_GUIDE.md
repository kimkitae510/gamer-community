# UI 마이그레이션 가이드

## 완료된 컴포넌트
✅ Header
✅ HomePage
✅ LoginPage
✅ JoinPage
✅ PostListPage
✅ CategoryTabs

## 공통 UI 컴포넌트
- Button (`src/components/ui/Button.tsx`)
- Card, CardHeader, CardBody, CardFooter (`src/components/ui/Card.tsx`)
- Badge (`src/components/ui/Badge.tsx`)
- Input (`src/components/ui/Input.tsx`)

## 변경 패턴

### 1. 색상 변경
**이전:**
```tsx
bg-[#2c3e6f]  // 네이비
bg-[#d5d5d5]  // 회색 보더
bg-[#f4f4f4]  // 배경
```

**이후:**
```tsx
bg-primary-600     // 메인 컬러
border-neutral-200 // 보더
bg-neutral-50      // 배경
```

### 2. 버튼 스타일
**이전:**
```tsx
<button className="px-4 py-2 bg-[#2c3e6f] text-white hover:bg-[#1f2d54]">
  클릭
</button>
```

**이후:**
```tsx
<Button variant="primary" size="md">
  클릭
</Button>
```

### 3. 카드 스타일
**이전:**
```tsx
<div className="bg-white border border-[#d5d5d5]">
  <div className="bg-[#2c3e6f] text-white px-6 py-4">헤더</div>
  <div className="p-6">내용</div>
</div>
```

**이후:**
```tsx
<Card>
  <CardHeader>헤더</CardHeader>
  <CardBody>내용</CardBody>
</Card>
```

### 4. 배지/태그
**이전:**
```tsx
<span className="px-2 py-1 bg-blue-600 text-white text-xs">
  태그
</span>
```

**이후:**
```tsx
<Badge variant="primary" size="sm">
  태그
</Badge>
```

### 5. 입력 필드
**이전:**
```tsx
<input className="w-full px-3 py-2 border border-[#d5d5d5] focus:border-[#2c3e6f]" />
```

**이후:**
```tsx
<Input 
  label="라벨"
  placeholder="입력하세요"
  helperText="도움말 텍스트"
/>
```

### 6. 레이아웃 컨테이너
**이전:**
```tsx
<div className="max-w-7xl mx-auto px-4">
  내용
</div>
```

**이후:**
```tsx
<div className="container-custom">
  내용
</div>
```

### 7. 그라디언트 배경
**Hero 섹션:**
```tsx
<section className="bg-gradient-to-br from-primary-600 via-primary-700 to-primary-800 text-white">
  <div className="container-custom py-16">
    {/* 내용 */}
  </div>
</section>
```

**페이지 배경:**
```tsx
<div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-50">
  {/* 내용 */}
</div>
```

### 8. 호버 효과
**카드:**
```tsx
<Card hover className="cursor-pointer">
  {/* 자동으로 hover:shadow-lg hover:-translate-y-0.5 적용 */}
</Card>
```

**링크:**
```tsx
<Link className="text-neutral-900 hover:text-primary-600 transition-colors">
  링크
</Link>
```

## 남은 작업

### 우선순위 높음
- [ ] PostDetailPage (게시글 상세)
- [ ] PostWritePage (글쓰기)
- [ ] PostEditPage (글수정)
- [ ] CommentSection (댓글)

### 우선순위 중간
- [ ] CategoryBoardPage (카테고리 메인)
- [ ] GameCardGrid (게임 카드)
- [ ] GenreSidebar (장르 사이드바)
- [ ] PopularBoardsSidebar (인기 게시판)

### 우선순위 낮음
- [ ] ReviewPage (리뷰 페이지)
- [ ] CategoryCreatePage (카테고리 생성)

## 컬러 팔레트

### Primary (파란색)
- 50: `primary-50` - 매우 밝은 배경
- 100: `primary-100` - 밝은 배경
- 200: `primary-200` - 포커스 링
- 500: `primary-500` - 포커스 보더
- 600: `primary-600` - 메인 컬러
- 700: `primary-700` - 호버 컬러

### Neutral (회색)
- 50: `neutral-50` - 페이지 배경
- 100: `neutral-100` - 구분선
- 200: `neutral-200` - 보더
- 300: `neutral-300` - 입력 필드 보더
- 600: `neutral-600` - 부가 텍스트
- 700: `neutral-700` - 라벨 텍스트
- 900: `neutral-900` - 제목 텍스트

### Badge Variants
- `primary`: 파란색 (기본)
- `secondary`: 회색 (일반)
- `success`: 초록색 (정보)
- `warning`: 노란색 (공략)
- `danger`: 빨간색 (HOT)
- `info`: 하늘색 (질문)

## 반응형 브레이크포인트
- `sm`: 640px
- `md`: 768px
- `lg`: 1024px
- `xl`: 1280px

## 아이콘
Heroicons SVG 사용:
```tsx
<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
</svg>
```

## 유틸리티 클래스
- `card-shadow`: 카드 그림자
- `card-shadow-hover`: 호버 시 카드 그림자
- `scrollbar-hide`: 스크롤바 숨김
- `container-custom`: 표준 컨테이너

## 예제: 전체 페이지 구조
```tsx
import { Card, CardBody, Button, Badge } from "../../components/ui";

export default function ExamplePage() {
  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero 섹션 (선택사항) */}
      <section className="bg-gradient-to-br from-primary-600 to-primary-800 text-white">
        <div className="container-custom py-16">
          <h1 className="text-4xl font-bold mb-4">제목</h1>
          <p className="text-xl text-primary-100 mb-8">설명</p>
          <Button variant="primary">행동 버튼</Button>
        </div>
      </section>

      {/* 메인 콘텐츠 */}
      <div className="container-custom py-8">
        <Card>
          <CardBody>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">
              섹션 제목
            </h2>
            {/* 내용 */}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}
```
