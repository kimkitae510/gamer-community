// src/pages/posts/PostListPage.tsx
import { Link, useParams, useNavigate, useSearchParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { postService, categoryService, authService, trendingService, topCategoryService } from "../../api/services";
import { formatPostDate, isEdited } from "../../utils/dateFormat";
import { useSidebarData } from "../../hooks/useSidebarData";
import TrendingPostsSidebar from "../../components/sidebar/TrendingPostsSidebar";
import TopCategoriesSidebar from "../../components/sidebar/TopCategoriesSidebar";
import type { Post, PostTag, PostSort, TopCategory } from "../../api/types";
import type { TrendingPost } from "../../api/types/trending.types";

const TAGS: ("전체" | PostTag)[] = ["전체", "일반", "질문", "정보", "공략"];
const SORT_OPTIONS: { label: string; value: PostSort }[] = [
  { label: "최신순", value: "LATEST" },
  { label: "인기순", value: "LIKED" },
  { label: "조회순", value: "VIEWED" },
  { label: "댓글순", value: "COMMENTED" },
];

export default function PostListPage() {
  const { categoryName, subCategoryId } = useParams<{ categoryName: string; subCategoryId: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [posts, setPosts] = useState<Post[]>([]);
  const [categoryNameDisplay, setCategoryNameDisplay] = useState<string>("");
  const [activeCategoryWritable, setActiveCategoryWritable] = useState(false);
  const [categoryRating, setCategoryRating] = useState<number>(0);
  const [categoryRatingCount, setCategoryRatingCount] = useState<number>(0);
  const [currentUser, setCurrentUser] = useState<string | null>(null);

  // 사이드바 데이터 (공통 훅)
  const { trendingPosts, topCategories, selectedPeriod, handlePeriodChange } = useSidebarData();

  // 페이징 상태
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const [selectedTagFilter, setSelectedTagFilter] = useState<string>(searchParams.get("tag") || "전체");
  const [selectedSort, setSelectedSort] = useState<PostSort>((searchParams.get("sort") as PostSort) || "LATEST");

  // URL 파라미터에서 페이지 읽기
  useEffect(() => {
    const tag = searchParams.get("tag") || "전체";
    const sort = (searchParams.get("sort") as PostSort) || "LATEST";
    const pageParam = parseInt(searchParams.get("page") || "0");
    setSelectedTagFilter(tag);
    setSelectedSort(sort);
    setPage(pageParam);
  }, [searchParams]);

  useEffect(() => {
    if (!subCategoryId) return;
    categoryService.getChildById(Number(subCategoryId))
      .then((category) => {
        setCategoryNameDisplay(category.name);
        setActiveCategoryWritable(category.writable);
        setCategoryRating(category.rating || 0);
        setCategoryRatingCount(category.ratingCount || 0);
      })
      .catch(() => setCategoryNameDisplay("알 수 없는 카테고리"));
  }, [subCategoryId]);

  // 페이지 로딩
  useEffect(() => {
    if (!subCategoryId) return;
    
    setLoading(true);

    postService.getByCategoryPaged({
      categoryId: Number(subCategoryId),
      tag: selectedTagFilter === "전체" ? null : (selectedTagFilter as PostTag),
      sort: selectedSort,
      page,
    })
    .then((data) => {
      setPosts(data.content);
      setTotalPages(data.totalPages);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    })
    .catch(console.error)
    .finally(() => setLoading(false));
  }, [subCategoryId, selectedTagFilter, selectedSort, page]);

  useEffect(() => {
    authService.getMe()
      .then((user) => setCurrentUser(user.loginId))
      .catch(() => setCurrentUser(null));
  }, []);

  //< 태그 필터 변경
  const handleTagClick = (tag: string) => {
    setSelectedTagFilter(tag);
    searchParams.set("tag", tag);
    searchParams.set("page", "0");
    setSearchParams(searchParams);
  };

  //< 정렬 변경
  const handleSortClick = (sort: PostSort) => {
    setSelectedSort(sort);
    searchParams.set("sort", sort);
    searchParams.set("page", "0");
    setSearchParams(searchParams);
  };

  //< 페이지 이동
  const handlePageClick = (newPage: number) => {
    searchParams.set("page", newPage.toString());
    setSearchParams(searchParams);
  };

  //< 게시글 삭제 후 현재 페이지 갱신
  const deletePost = async (postId: number) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await postService.delete(postId);
      
      // 삭제 후 현재 페이지 다시 로드
      const data = await postService.getByCategoryPaged({
        categoryId: Number(subCategoryId),
        tag: selectedTagFilter === "전체" ? null : (selectedTagFilter as PostTag),
        sort: selectedSort,
        page,
      });
      
      setPosts(data.content);
      setTotalPages(data.totalPages);
      
      // 현재 페이지에 게시글이 없으면 이전 페이지로
      if (data.content.length === 0 && page > 0) {
        handlePageClick(page - 1);
      }
      
      alert("게시글이 삭제되었습니다.");
    } catch (error) {
      console.error("게시글 삭제 실패:", error);
      alert("게시글 삭제에 실패했습니다.");
    }
  };

  //< 페이지네이션 버튼 렌더링 (최대 5개 표시)
  const renderPagination = () => {
    if (totalPages <= 1) return null;

    const pageNumbers = [];
    const maxButtons = 5;
    let startPage = Math.max(0, page - 2);
    let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

    if (endPage - startPage < maxButtons - 1) {
      startPage = Math.max(0, endPage - maxButtons + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pageNumbers.push(i);
    }

    return (
      <div className="flex items-center justify-center gap-2 mt-8">
        {/* 이전 버튼 */}
        <button
          onClick={() => handlePageClick(page - 1)}
          disabled={page === 0}
          className="px-3 py-2 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-md hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          이전
        </button>

        {/* 페이지 번호 버튼 */}
        {pageNumbers.map((pageNum) => (
          <button
            key={pageNum}
            onClick={() => handlePageClick(pageNum)}
            className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
              page === pageNum
                ? "bg-primary-600 text-white shadow-md"
                : "text-neutral-700 bg-white border border-neutral-300 hover:bg-neutral-50"
            }`}
          >
            {pageNum + 1}
          </button>
        ))}

        {/* 다음 버튼 */}
        <button
          onClick={() => handlePageClick(page + 1)}
          disabled={page >= totalPages - 1}
          className="px-3 py-2 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-md hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          다음
        </button>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-[1400px] ml-32 px-4 py-8">
        <div className="flex gap-6">
          {/* 왼쪽 사이드바 */}
          <aside className="hidden lg:block w-80 flex-shrink-0 space-y-6">
            <TrendingPostsSidebar trendingPosts={trendingPosts} />
            <TopCategoriesSidebar
              topCategories={topCategories}
              selectedPeriod={selectedPeriod}
              onPeriodChange={handlePeriodChange}
            />
          </aside>

          {/* 메인 컨텐츠 */}
          <main className="w-full max-w-4xl ml-8">
            {/* 헤더 */}
            <div className="bg-white rounded-xl shadow-sm border border-neutral-200 p-6 mb-6">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h1 className="text-3xl font-bold text-neutral-900 mb-2">{categoryNameDisplay}</h1>
                  <div className="flex items-center gap-4">
                    {/* 평점 표시 */}
                    <Link
                      to={`/${categoryName}/game/${subCategoryId}/reviews`}
                      className="flex items-center gap-2 text-sm hover:opacity-70 transition-opacity"
                    >
                      <span className="text-gray-600">평점 :</span>
                      <span className="text-primary-600 font-bold text-base hover:underline">
                        {categoryRating.toFixed(1)}
                      </span>
                      <span className="text-gray-400">
                        ({categoryRatingCount}개의 리뷰)
                      </span>
                    </Link>
                  </div>
                </div>
                
                {/* 리뷰 버튼 */}
                <Link
                  to={`/${categoryName}/game/${subCategoryId}/reviews`}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
                >
                  리뷰 보기 / 작성하기
                </Link>
              </div>
              
              {/* 필터 바 */}
              <div className="border-t border-neutral-200 pt-4">
                <div className="flex items-center justify-between">
                  {/* 태그 필터 */}
                  <div className="flex gap-2">
                    {TAGS.map(tag => (
                      <button
                        key={tag}
                        onClick={() => handleTagClick(tag)}
                        className={`px-4 py-2 text-sm font-medium transition-all rounded-lg ${
                          selectedTagFilter === tag 
                            ? "bg-primary-600 text-white shadow-md" 
                            : "bg-neutral-100 text-neutral-700 hover:bg-neutral-200"
                        }`}
                      >
                        {tag}
                      </button>
                    ))}
                  </div>

                  {/* 정렬 + 글쓰기 */}
                  <div className="flex items-center gap-3">
                    <select
                      value={selectedSort}
                      onChange={(e) => handleSortClick(e.target.value as PostSort)}
                      className="px-4 py-2 text-sm border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
                    >
                      {SORT_OPTIONS.map(opt => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                      ))}
                    </select>

                    <Link
                      to={`/${categoryName}/game/${subCategoryId}/posts/write`}
                      className={`px-5 py-2 text-sm font-medium rounded-lg transition-colors ${
                        activeCategoryWritable 
                          ? "bg-primary-600 text-white hover:bg-primary-700 shadow-md" 
                          : "bg-gray-300 text-gray-500 cursor-not-allowed"
                      }`}
                      onClick={(e) => { if (!activeCategoryWritable) e.preventDefault(); }}
                    >
                      글쓰기
                    </Link>
                  </div>
                </div>
              </div>
            </div>

            {/* 로딩 중 */}
            {loading ? (
              <div className="bg-white rounded-xl shadow-sm border border-neutral-200 p-20 text-center">
                <div className="animate-spin w-10 h-10 mx-auto border-4 border-neutral-200 border-t-primary-600 rounded-full"></div>
                <p className="text-neutral-500 mt-4">로딩 중...</p>
              </div>
            ) : posts.length === 0 ? (
              <div className="bg-white rounded-xl shadow-sm border border-neutral-200 p-20 text-center">
                <div className="text-6xl mb-4">📝</div>
                <p className="text-neutral-500 mb-4">게시글이 없습니다</p>
                {activeCategoryWritable && (
                  <Link
                    to={`/${categoryName}/game/${subCategoryId}/posts/write`}
                    className="inline-block px-6 py-3 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 shadow-md transition-colors"
                  >
                    첫 게시글 작성하기
                  </Link>
                )}
              </div>
            ) : (
              <>
                <div className="bg-white rounded-xl shadow-sm border border-neutral-200 overflow-hidden">
                  <div className="divide-y divide-neutral-100">
                    {posts.map((post) => (
                      <div key={post.id} className="hover:bg-neutral-50 transition-colors">
                        <Link 
                          to={`/${categoryName}/game/${subCategoryId}/posts/view/${post.id}`}
                          className="block p-4"
                        >
                          <div className="flex items-start justify-between gap-4">
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center gap-2 mb-2">
                                <span className="inline-block px-2 py-0.5 text-xs font-semibold text-primary-700 bg-primary-50 rounded-full">
                                  {post.tag}
                                </span>
                              </div>
                              
                              <h3 className="text-base font-semibold text-neutral-900 mb-2.5 hover:text-primary-600 transition-colors line-clamp-2">
                                {post.title}
                                {post.commentCount > 0 && (
                                  <span className="ml-2 text-sm text-primary-600 font-bold">[{post.commentCount}]</span>
                                )}
                              </h3>
                              
                              <div className="flex items-center gap-2 text-sm text-neutral-500">
                                <span className="font-medium text-neutral-700">{post.author}</span>
                                <span>·</span>
                                <span>{formatPostDate(post.createdAt)}</span>
                                {isEdited(post.createdAt, post.updatedAt) && (
                                  <>
                                    <span>·</span>
                                    <span className="text-neutral-400" title={new Date(post.updatedAt).toLocaleString("ko-KR")}>수정됨</span>
                                  </>
                                )}
                              </div>
                            </div>

                            <div className="flex items-center gap-3 text-sm text-neutral-500 shrink-0">
                              <div className="flex items-center gap-1">
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                <span>{post.views}</span>
                              </div>
                              <div className="flex items-center gap-1 text-neutral-500">
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                                </svg>
                                <span>{post.likeCount}</span>
                              </div>
                            </div>
                          </div>
                        </Link>

                        {/* 관리 버튼 */}
                        {currentUser === post.authorId && (
                          <div className="flex justify-end gap-2 px-4 pb-3">
                            <Link 
                              to={`/${categoryName}/game/${subCategoryId}/posts/edit/${post.id}`}
                              className="px-2.5 py-1 text-xs text-neutral-600 hover:text-primary-600 hover:bg-primary-50 rounded-md transition-colors"
                            >
                              수정
                            </Link>
                            <button 
                              onClick={(e) => {
                                e.preventDefault();
                                deletePost(post.id);
                              }}
                              className="px-2.5 py-1 text-xs text-neutral-600 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
                            >
                              삭제
                            </button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                {/* 페이지네이션 */}
                {renderPagination()}
              </>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
