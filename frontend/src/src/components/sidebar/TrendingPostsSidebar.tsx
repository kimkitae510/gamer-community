import { Link } from "react-router-dom";
import type { TrendingPost } from "../../api/types/trending.types";

interface Props {
  trendingPosts: TrendingPost[];
}

export default function TrendingPostsSidebar({ trendingPosts }: Props) {
  return (
    <div className="bg-white rounded-lg border border-neutral-200 p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-base font-bold text-neutral-900">실시간 인기글</h3>
        <Link to="/trending" className="text-sm text-primary-600 hover:text-primary-700">
          더보기
        </Link>
      </div>
      <div className="divide-y divide-neutral-100">
        {trendingPosts.slice(0, 10).map((post) => (
          <Link
            key={post.postId}
            to={`/${post.categoryName}/game/${post.categoryId}/posts/view/${post.postId}`}
            className="block hover:bg-neutral-50 py-2.5 px-2 rounded transition-colors"
          >
            <p className="text-sm font-semibold text-neutral-900 line-clamp-1 hover:text-primary-600 mb-1">
              {post.title}
              {post.commentCount > 0 && (
                <span className="ml-1 text-xs text-primary-600 font-medium">[{post.commentCount}]</span>
              )}
            </p>
            <span className="text-sm text-neutral-600">{post.categoryName} 게시판</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
