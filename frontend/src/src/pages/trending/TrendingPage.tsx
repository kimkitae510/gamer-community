import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { trendingService } from "../../api/services";
import { formatSmartDate } from "../../utils/dateFormat";
import type { TrendingPost } from "../../api/types/trending.types";

export default function TrendingPage() {
  const [trendingPosts, setTrendingPosts] = useState<TrendingPost[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    trendingService
      .getAllTrending()
      .then(setTrendingPosts)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* 헤더 */}
      <div className="bg-white border-b border-neutral-200">
        <div className="container-custom py-8">
          <div className="flex items-center gap-2 mb-2">
            <span className="inline-block px-2.5 py-1 bg-red-600 text-white text-xs font-bold rounded shadow-sm">
              HOT
            </span>
            <h1 className="text-3xl font-bold text-neutral-900">
              실시간 인기글
            </h1>
          </div>
          <p className="text-neutral-600">
            다양한 게시판의 과거 인기글들까지 모아보세요!
          </p>
        </div>
      </div>

      {/* 컨텐츠 */}
      <div className="container-custom py-8">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="animate-spin w-10 h-10 border-3 border-neutral-200 border-t-primary-600 rounded-full"></div>
          </div>
        ) : trendingPosts.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-6xl mb-4">📭</div>
            <h3 className="text-xl font-semibold text-neutral-900 mb-2">
              실시간 인기글이 없습니다
            </h3>
            <p className="text-neutral-500 mb-6">
              24시간 이내 100점 이상을 달성한 게시글이 없습니다
            </p>
            <Link
              to="/"
              className="inline-block px-6 py-3 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors"
            >
              홈으로 돌아가기
            </Link>
          </div>
        ) : (
          <>
            {/* 게시글 리스트 */}
            <div className="bg-white rounded-lg border border-neutral-200 divide-y divide-neutral-100">
              {trendingPosts.map((post) => (
                <Link
                  key={post.postId}
                  to={`/${post.categoryName}/game/${post.categoryId}/posts/view/${post.postId}`}
                  className="block hover:bg-neutral-50 transition-colors px-4 py-3"
                >
                  {/* 제목 + 댓글 */}
                  <h2 className="font-semibold text-[15px] text-neutral-900 mb-1.5 hover:text-primary-600 transition-colors line-clamp-1">
                    {post.title}{' '}
                    <span className="text-primary-600 font-medium text-sm">
                      [{post.commentCount}]
                    </span>
                  </h2>

                  {/* 메타 정보 */}
                  <div className="flex items-center gap-2 text-xs text-neutral-500">
                    <span>{post.categoryName}</span>
                    <span className="text-neutral-300">|</span>
                    <span>조회 {post.views}</span>
                    <span className="text-primary-600 font-medium">추천 {post.likeCount}</span>
                    <span className="text-neutral-400">{formatSmartDate(post.createdAt)}</span>
                    <span className="ml-auto">{post.author}</span>
                  </div>
                </Link>
              ))}
            </div>

            {/* 하단 안내 문구 */}
            <div className="text-center py-8">
              <p className="text-sm text-neutral-500">
                작성자가 원본 게시글을 삭제할 경우, 해당 페이지에서도 게시글이 삭제됩니다
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
