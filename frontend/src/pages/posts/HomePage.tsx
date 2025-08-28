import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { postService, newsService, trendingService, topCategoryService, categoryService, type TopCategory } from "../../api/services";
import { formatPostDate, formatNewsDate } from "../../utils/dateFormat";
import { Card, CardBody } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import type { Post, NewsItem, Platform, Category } from "../../api/types";
import type { TrendingPost } from "../../api/types/trending.types";
import gameBanner from "../../assets/game.jpg";

const platforms = [
  { label: "전체", value: null as Platform | null },
  { label: "PlayStation", value: "PLAYSTATION" as Platform },
  { label: "Nintendo", value: "NINTENDO" as Platform },
  { label: "Xbox", value: "XBOX" as Platform },
  { label: "PC", value: "PC" as Platform },
  { label: "Mobile", value: "MOBILE" as Platform },
];

export default function HomePage() {
  //< 상태 관리
  const [trendingPosts, setTrendingPosts] = useState<TrendingPost[]>([]); //< 인기글 목록
  const [news, setNews] = useState<NewsItem[]>([]); //< 뉴스 목록
  const [selectedPlatform, setSelectedPlatform] = useState<Platform | null>(null); //< 선택된 플랫폼 필터
  const [loadingNews, setLoadingNews] = useState(false);
  const [topCategories, setTopCategories] = useState<TopCategory[]>([]); //< 인기 게시판 목록
  const [selectedPeriod, setSelectedPeriod] = useState<'daily' | 'weekly' | 'monthly'>('daily'); //< 기간 필터
  const [newCategories, setNewCategories] = useState<Category[]>([]); //< 신규 게시판 목록

  //< 초기 데이터 로드 (인기글, 인기 게시판, 신규 게시판)
  useEffect(() => {
    trendingService.getTop10Trending().then(setTrendingPosts).catch(console.error);
    topCategoryService.getDailyTop().then(setTopCategories).catch(console.error);
    categoryService.getNewCategories().then(setNewCategories).catch(console.error);
  }, []);

  //< 플랫폼 필터 변경 시 뉴스 재조회
  useEffect(() => {
    newsService
      .getNews(selectedPlatform, 8)
      .then((data) => {
        if (selectedPlatform) {
          const filtered = data.filter(item => item.platform === selectedPlatform);
          setNews(filtered.slice(0, 8));
        } else {
          setNews(data.slice(0, 8));
        }
      })
      .catch(console.error)
      .finally(() => setLoadingNews(false));
  }, [selectedPlatform]);

  //< 인기 게시판 기간 필터 변경 핸들러
  const handlePeriodChange = async (period: 'daily' | 'weekly' | 'monthly') => {
    setSelectedPeriod(period);
    try {
      switch (period) {
        case 'daily':
          setTopCategories(await topCategoryService.getDailyTop());
          break;
        case 'weekly':
          setTopCategories(await topCategoryService.getWeeklyTop());
          break;
        case 'monthly':
          setTopCategories(await topCategoryService.getMonthlyTop());
          break;
      }
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      {/* 게임 배너 이미지 */}
      <section className="relative bg-black">
        <div className="w-full overflow-hidden">
          <img 
            src={gameBanner} 
            alt="Gaming Banner" 
            className="w-full h-auto object-cover opacity-40"
            style={{ maxHeight: '400px' }}
          />
        </div>
        {/* 배너 텍스트 오버레이 - 왼쪽 하단 */}
        <div className="absolute inset-0 flex items-end pb-12 md:pb-16">
          <div className="container-custom w-full">
            <div className="max-w-4xl">
              <h1 className="text-white font-black text-4xl md:text-5xl lg:text-6xl mb-4 leading-tight">
                Gamer Community에서<br />
                모든 게임 이야기를 나누세요
              </h1>
              <p className="text-white/90 text-lg md:text-xl font-medium">
                PlayStation, Xbox, Nintendo, PC, Mobile 모든 플랫폼의 게이머들이 모이는 곳
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 게임 뉴스 섹션 */}
      <section className="bg-white py-8">
        <div className="container-custom">
          {/* 제목 */}
          <div className="mb-6">
            <h2 className="text-2xl font-bold mb-1 text-neutral-900">
              게임 뉴스
            </h2>
            <p className="text-sm text-neutral-600">
              Gamer Community에서 최신 게임 뉴스를 읽어보세요
            </p>
          </div>

          {/* 플랫폼 필터 - Pill 스타일 */}
          <div className="flex gap-2 mb-6 overflow-x-auto pb-2">
            {platforms.map((platform) => (
              <button
                key={platform.value || 'all'}
                onClick={() => setSelectedPlatform(platform.value)}
                className={`px-4 py-1.5 text-sm font-medium whitespace-nowrap transition-all rounded-full ${
                  selectedPlatform === platform.value
                    ? "bg-primary-600 text-white shadow-md"
                    : "bg-white text-neutral-700 border border-neutral-300 hover:border-primary-600"
                }`}
              >
                {platform.label}
              </button>
            ))}
          </div>

          {/* 뉴스 카드 그리드 */}
          {loadingNews ? (
            <div className="flex items-center justify-center py-20">
              <div className="animate-spin w-10 h-10 border-3 border-neutral-200 border-t-primary-600 rounded-full"></div>
            </div>
          ) : news.length === 0 ? (
            <div className="text-center py-16">
              <p className="text-neutral-500">뉴스가 없습니다</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {news.map((item) => (
                <a
                  key={item.id}
                  href={item.originalUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group bg-white rounded-lg overflow-hidden border border-neutral-200 hover:shadow-md transition-all"
                >
                  {/* 이미지 */}
                  <div className="relative aspect-video bg-neutral-100 overflow-hidden">
                    {item.imageUrl ? (
                      <>
                        <img
                          src={item.imageUrl}
                          alt={item.title}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                          onError={(e) => {
                            e.currentTarget.style.display = 'none';
                            const parent = e.currentTarget.parentElement;
                            if (parent) {
                              const fallback = parent.querySelector('.fallback-image');
                              if (fallback) {
                                (fallback as HTMLElement).style.display = 'flex';
                              }
                            }
                          }}
                        />
                        <div className="fallback-image w-full h-full hidden flex-col items-center justify-center bg-neutral-50 absolute inset-0">
                          <span className="text-4xl opacity-20 mb-2">🎮</span>
                          <span className="text-neutral-400 text-sm font-medium">No Image</span>
                        </div>
                      </>
                    ) : (
                      <div className="w-full h-full flex flex-col items-center justify-center bg-neutral-50">
                        <span className="text-4xl opacity-20 mb-2">🎮</span>
                        <span className="text-neutral-400 text-sm font-medium">No Image</span>
                      </div>
                    )}
                    {/* 플랫폼 배지 */}
                    <div className="absolute top-2 left-2">
                      <span className="inline-block px-2 py-0.5 bg-black/60 backdrop-blur-sm text-white text-xs font-medium rounded">
                        {platforms.find(p => p.value === item.platform)?.label || "게임"}
                      </span>
                    </div>
                  </div>

                  {/* 내용 */}
                  <div className="p-3">
                    <h3 className="font-semibold text-sm text-neutral-900 mb-1.5 line-clamp-2 leading-tight group-hover:text-primary-600 transition-colors">
                      {item.title}
                    </h3>
                    <p className="text-xs text-neutral-500 mb-1 line-clamp-1">
                      {item.content?.substring(0, 50) || item.source || "게임 뉴스"}
                    </p>
                    {/* 날짜 표시 */}
                    <div className="flex items-center text-xs text-neutral-400">
                      <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span>{formatNewsDate(item.publishedAt)}</span>
                    </div>
                  </div>
                </a>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* 실시간 인기글 & 인기 게시판 섹션 */}
      <section className="bg-white py-8">
        <div className="container-custom">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* 왼쪽: 실시간 인기글 */}
            <div>
              {/* 제목 */}
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold text-neutral-900 mb-2">
                    실시간 인기글
                  </h2>
                  <p className="text-sm text-neutral-500">
                    다양한 게시판의 인기글들을 확인해보세요
                  </p>
                </div>
                
                {/* 실시간 인기글 게시판 링크 */}
                <Link
                  to="/trending"
                  className="inline-flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700 font-medium whitespace-nowrap"
                >
                  더보기
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </Link>
              </div>

              {/* 인기글 리스트 */}
              {trendingPosts.length === 0 ? (
                <div className="text-center py-16">
                  <p className="text-neutral-500">실시간 인기글이 없습니다</p>
                </div>
              ) : (
                <div className="bg-white rounded-lg border border-neutral-200 overflow-hidden">
                  <div className="divide-y divide-neutral-100">
                    {trendingPosts.slice(0, 10).map((post) => (
                      <Link
                        key={post.postId}
                        to={`/${post.categoryName}/game/${post.categoryId}/posts/view/${post.postId}`}
                        className="block hover:bg-neutral-50 transition-colors px-4 py-3"
                      >
                        {/* 제목 + 댓글 */}
                        <h3 className="font-medium text-[15px] text-neutral-900 mb-1.5 hover:text-primary-600 transition-colors line-clamp-1">
                          {post.title}{' '}
                          <span className="text-primary-600 font-medium text-sm">
                            [{post.commentCount}]
                          </span>
                        </h3>

                        {/* 메타 정보 */}
                        <div className="flex items-center gap-2 text-xs text-neutral-500">
                          <span>{post.categoryName}</span>
                          <span className="text-neutral-300">|</span>
                          <span>조회 {post.views}</span>
                          <span className="text-primary-600 font-medium">추천 {post.likeCount}</span>
                          <span className="ml-auto">{post.author}</span>
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* 오른쪽: 인기 게시판 */}
            <div>
              {/* 제목 및 탭 */}
              {/* 제목 및 탭 */}
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold text-neutral-900 mb-2">
                    인기 게시판
                  </h2>
                  <p className="text-sm text-neutral-500">
                    오늘, 이번 주, 이번 달 인기 글을 확인해보세요
                  </p>
                </div>
                
                {/* 기간 선택 탭 */}
                <div className="flex gap-2">
                  <button
                    onClick={() => handlePeriodChange('daily')}
                    className={`px-3 py-1 text-sm font-medium rounded-full transition-all ${
                      selectedPeriod === 'daily'
                        ? 'bg-primary-600 text-white shadow-md'
                        : 'bg-white text-neutral-700 border border-neutral-300 hover:border-primary-600'
                    }`}
                  >
                    일간
                  </button>
                  <button
                    onClick={() => handlePeriodChange('weekly')}
                    className={`px-3 py-1 text-sm font-medium rounded-full transition-all ${
                      selectedPeriod === 'weekly'
                        ? 'bg-primary-600 text-white shadow-md'
                        : 'bg-white text-neutral-700 border border-neutral-300 hover:border-primary-600'
                    }`}
                  >
                    주간
                  </button>
                  <button
                    onClick={() => handlePeriodChange('monthly')}
                    className={`px-3 py-1 text-sm font-medium rounded-full transition-all ${
                      selectedPeriod === 'monthly'
                        ? 'bg-primary-600 text-white shadow-md'
                        : 'bg-white text-neutral-700 border border-neutral-300 hover:border-primary-600'
                    }`}
                  >
                    월간
                  </button>
                </div>
              </div>

              {topCategories.length === 0 ? (
                <div className="text-center py-16">
                  <p className="text-neutral-500">인기 게시판이 없습니다</p>
                </div>
              ) : (
                <div className="border border-neutral-200 rounded-lg overflow-hidden bg-white">
                  {/* 1위 - 큰 카드 */}
                  {topCategories[0] && (
                    <Link
                      to={`/${topCategories[0].categoryName}/game/${topCategories[0].categoryId}`}
                      className="block p-5 bg-neutral-50 border-b border-neutral-200 hover:bg-neutral-100 transition-colors"
                    >
                      <div className="flex gap-4 items-center">
                        {/* 순위 배지 */}
                        <div className="flex-shrink-0">
                          <div className="w-10 h-10 bg-primary-600 rounded flex items-center justify-center text-white font-black text-lg">
                            1
                          </div>
                        </div>

                        {/* 게임 이미지 */}
                        <div className="flex-shrink-0">
                          <img
                            src={topCategories[0].imageUrl || '/placeholder-game.jpg'}
                            alt={topCategories[0].categoryName}
                            className="w-16 h-16 rounded object-cover"
                            onError={(e) => {
                              e.currentTarget.src = '/placeholder-game.jpg';
                            }}
                          />
                        </div>

                        {/* 정보 */}
                        <div className="flex-1 min-w-0">
                          {/* 게임명 */}
                          <h3 className="text-base font-bold text-neutral-900 mb-2 truncate">
                            {topCategories[0].categoryName}
                          </h3>

                          <div className="flex flex-col gap-1.5">
                            {/* 평점 */}
                            <div className="flex items-center gap-1">
                              <span className="text-sm text-neutral-600">평점 :</span>
                              <button
                                className="font-bold text-red-600 hover:text-red-700 hover:underline underline-offset-2 transition-colors"
                                onClick={(e) => {
                                  e.preventDefault();
                                  e.stopPropagation();
                                  window.location.href = `/${topCategories[0].categoryName}/game/${topCategories[0].categoryId}/reviews`;
                                }}
                              >
                                {topCategories[0].rating.toFixed(1)} <span className="text-xs text-neutral-500 font-normal">({topCategories[0].ratingCount}개의 리뷰)</span>
                              </button>
                            </div>

                            {/* 장르 - 태그 형태 */}
                            <div className="flex gap-1">
                              {topCategories[0].genres.slice(0, 2).map((genre, idx) => (
                                <span
                                  key={idx}
                                  className="text-xs text-neutral-600 bg-neutral-100 px-2 py-0.5 rounded"
                                >
                                  {genre}
                                </span>
                              ))}
                            </div>
                          </div>
                        </div>

                        {/* 새글 수 */}
                        <div className="flex-shrink-0 text-right">
                          <div className="text-sm text-neutral-600">
                            글 <span className="font-bold text-neutral-900">{topCategories[0].postCount}</span>
                          </div>
                        </div>
                      </div>
                    </Link>
                  )}

                  {/* 2~7위 - 리스트 */}
                  <div className="divide-y divide-neutral-100">
                    {topCategories.slice(1, 7).map((category) => (
                      <Link
                        key={category.categoryId}
                        to={`/${category.categoryName}/game/${category.categoryId}`}
                        className="flex items-center gap-4 px-5 py-3 hover:bg-neutral-50 transition-colors"
                      >
                        {/* 순위 */}
                        <div className="flex-shrink-0 w-6 text-center">
                          <span className="text-base font-bold text-primary-600">
                            {category.rank}
                          </span>
                        </div>

                        {/* 게임명 */}
                        <div className="flex-1 min-w-0">
                          <h4 className="text-sm font-medium text-neutral-900 truncate">
                            {category.categoryName}
                          </h4>
                        </div>

                        {/* 평점 & 글 수 */}
                        <div className="flex-shrink-0 flex items-center gap-2 text-sm">
                          <div className="flex items-center gap-1">
                            <span className="text-neutral-600">평점 :</span>
                            <button
                              className="text-red-600 font-bold hover:text-red-700 hover:underline underline-offset-2 transition-colors"
                              onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                window.location.href = `/${category.categoryName}/game/${category.categoryId}/reviews`;
                              }}
                            >
                              {category.rating > 0 ? category.rating.toFixed(1) : '0.0'} <span className="text-neutral-600 font-normal text-xs">({category.ratingCount}개의 리뷰)</span>
                            </button>
                          </div>
                          <span className="text-neutral-300">|</span>
                          <div className="text-neutral-600">
                            글 <span className="font-bold text-neutral-900">{category.postCount}</span>
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 신설 게시판 섹션 */}
      <section className="bg-white py-8">
        <div className="container-custom">
          {/* 제목 */}
          <div className="mb-6">
            <h2 className="text-2xl font-bold mb-1 text-neutral-900">
              신규 게시판
            </h2>
            <p className="text-sm text-neutral-600">
              신규게시판에서 최신 정보를 가장 먼저 확인하세요
            </p>
          </div>

          {/* 게임 카드 그리드 */}
          {newCategories.length === 0 ? (
            <div className="text-center py-16">
              <p className="text-neutral-500">신설 게시판이 없습니다</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
              {newCategories.map((category) => (
                <Link
                  key={category.id}
                  to={`/${category.name}/game/${category.id}`}
                  className="group bg-white rounded-lg overflow-hidden border border-neutral-200 hover:shadow-lg transition-all"
                >
                  {/* 게임 이미지 */}
                  <div className="relative aspect-[3/4] bg-neutral-100 overflow-hidden">
                    {category.imageUrl ? (
                      <img
                        src={category.imageUrl}
                        alt={category.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        onError={(e) => {
                          e.currentTarget.src = '/placeholder-game.jpg';
                        }}
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-neutral-100">
                        <span className="text-6xl opacity-10">🎮</span>
                      </div>
                    )}
                  </div>

                  {/* 게임 정보 */}
                  <div className="p-4">
                    {/* 게임명 */}
                    <h3 className="font-bold text-base text-neutral-900 mb-1.5 line-clamp-2 leading-tight group-hover:text-primary-600 transition-colors min-h-[2.5rem]">
                      {category.name}
                    </h3>

                    {/* 평점 */}
                    <div className="mb-1 flex items-center gap-1">
                      <span className="text-sm text-neutral-600">평점</span>
                      <button
                        className="text-red-600 font-bold text-base hover:text-red-700 hover:underline underline-offset-2 transition-colors"
                        onClick={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          window.location.href = `/${category.name}/game/${category.id}/reviews`;
                        }}
                      >
                        {category.rating?.toFixed(1) || '0.0'} <span className="text-xs text-neutral-500 font-normal">({category.ratingCount || 0}개의 리뷰)</span>
                      </button>
                    </div>

                    {/* 글 수 */}
                    <div className="mb-2 flex items-center gap-1">
                      <span className="text-sm text-neutral-600">글</span>
                      <span className="font-bold text-neutral-900 text-base">{category.postCount || 0}</span>
                    </div>

                    {/* 장르 */}
                    {category.genres && category.genres.length > 0 && (
                      <div className="flex flex-wrap gap-1.5">
                        {category.genres.slice(0, 2).map((genre, idx) => (
                          <span
                            key={idx}
                            className="text-xs text-neutral-600 bg-neutral-100 px-2 py-1 rounded"
                          >
                            {genre.name}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>


    </div>
  );
}
