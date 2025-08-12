// src/pages/posts/PostDetailPage.tsx
import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { postService, authService, likeService, aiService } from "../../api/services";
import { formatSmartDate, formatDateTime, isEdited } from "../../utils/dateFormat";
import { useSidebarData } from "../../hooks/useSidebarData";
import TrendingPostsSidebar from "../../components/sidebar/TrendingPostsSidebar";
import TopCategoriesSidebar from "../../components/sidebar/TopCategoriesSidebar";
import type { Post, TopCategory } from "../../api/types";
import type { TrendingPost } from "../../api/types/trending.types";
import CommentSection from "../comments/CommentSection";

export default function PostDetailPage() {
  const { categoryName, subCategoryId, postId } = useParams<{
    categoryName: string;
    subCategoryId: string;
    postId: string;
  }>();

  const numericPostId = postId ? Number(postId) : null;
  const [post, setPost] = useState<Post | null>(null);
  const [currentUser, setCurrentUser] = useState<string | null>(null);
  const [likeStatus, setLikeStatus] = useState({ liked: false, likeCount: 0 });
  const [loading, setLoading] = useState(true);
  const { trendingPosts, topCategories, selectedPeriod, handlePeriodChange } = useSidebarData();
  const [aiAnswer, setAiAnswer] = useState<string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);
  const navigate = useNavigate();

  //< 게시글 + 사용자 + 좋아요 상태 조회
  useEffect(() => {
    if (!numericPostId) return;

    const fetchData = async () => {
      try {
        const postData = await postService.getById(numericPostId);
        setPost(postData);
        setLikeStatus({ liked: false, likeCount: postData.likeCount });

        try {
          const user = await authService.getMe();
          setCurrentUser(user.loginId);

          try {
            const likeStatusData = await likeService.getPostLikeStatus(numericPostId);
            setLikeStatus({ liked: likeStatusData.isLiked ?? likeStatusData.liked, likeCount: postData.likeCount });
          } catch {
            console.error("좋아요 상태 조회 실패");
          }
        } catch {
          setCurrentUser(null);
        }

        setLoading(false);
      } catch {
        alert("게시글을 불러오는 데 실패했습니다.");
        setLoading(false);
      }
    };

    fetchData();
  }, [numericPostId]);

  //< 게시글 삭제
  const deletePost = async () => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    if (!numericPostId) return;

    try {
      await postService.delete(numericPostId);
      alert("게시글이 삭제되었습니다.");
      navigate(`/${categoryName}/game/${subCategoryId}`);
    } catch {
      alert("삭제 실패했습니다.");
    }
  };

  //< 좋아요 토글 (낙관적 업데이트 적용)
  const toggleLike = async () => {
    if (!currentUser || !numericPostId) return;

    const prevLiked = likeStatus.liked;
    const prevCount = likeStatus.likeCount;

    setLikeStatus({
      liked: !prevLiked,
      likeCount: prevLiked ? prevCount - 1 : prevCount + 1,
    });

    try {
      const result = await likeService.togglePostLike(numericPostId);
      setLikeStatus({
        liked: result.isLiked ?? result.liked,
        likeCount: result.likeCount,
      });
    } catch (error) {
      console.error("좋아요 처리 실패:", error);
      setLikeStatus({ liked: prevLiked, likeCount: prevCount });
    }
  };

  //< 목록으로 돌아가기
  const goBack = () => {
    navigate(`/${categoryName}/game/${subCategoryId}`);
  };

  //< AI 답변 요청
  const askAi = async () => {
    if (!numericPostId) return;
    setAiLoading(true);
    setAiAnswer(null);
    try {
      const answer = await aiService.askAi(numericPostId);
      setAiAnswer(answer);
    } catch {
      setAiAnswer("AI 답변을 가져오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setAiLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin w-10 h-10 border-3 border-gray-200 border-t-blue-700 rounded-full mx-auto mb-4"></div>
          <p className="text-gray-600">게시글을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center max-w-md">
          <h2 className="text-xl font-bold text-gray-900 mb-2">게시글을 찾을 수 없습니다</h2>
          <p className="text-gray-600 mb-6">삭제되었거나 존재하지 않는 게시글입니다</p>
          <button
            onClick={goBack}
            className="px-6 py-2.5 bg-blue-700 text-white font-medium rounded-md hover:bg-blue-800"
          >
            목록으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-[1400px] ml-32 px-4 py-8">
        <div className="flex gap-6">
          {/* 왼쪽 사이드바 */}
          <aside className="hidden lg:block w-80 flex-shrink-0 space-y-6" style={{ marginTop: '40px' }}>
            <TrendingPostsSidebar trendingPosts={trendingPosts} />
            <TopCategoriesSidebar
              topCategories={topCategories}
              selectedPeriod={selectedPeriod}
              onPeriodChange={handlePeriodChange}
            />
          </aside>

          {/* 메인 컨텐츠 */}
          <main className="w-full max-w-4xl ml-8">
            {/* 뒤로가기 */}
            <button
              onClick={goBack}
              className="mb-4 text-sm text-gray-600 hover:text-gray-900"
            >
              목록으로
            </button>

            {/* 게시글 */}
            <article className="bg-white rounded-lg border border-neutral-200 p-6 mb-6">
              {/* 헤더 */}
              <div className="mb-6">
                <div className="flex items-center gap-2 mb-3">
                  <span className="inline-block px-2 py-0.5 text-xs font-semibold text-primary-700 bg-primary-50 rounded-full">
                    {post.tag}
                  </span>
                  {isEdited(post.createdAt, post.updatedAt) && (
                    <span className="text-xs text-gray-500" title={formatDateTime(post.updatedAt)}>
                      수정됨
                    </span>
                  )}
                </div>
                
                <h1 className="text-2xl font-bold text-gray-900 mb-4">
                  {post.title}
                </h1>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 text-sm text-gray-600">
                    <span className="font-medium text-gray-900">{post.author}</span>
                    <span>·</span>
                    <span>{formatSmartDate(post.createdAt)}</span>
                    <span>·</span>
                    <span>조회 {post.views.toLocaleString()}</span>
                  </div>

                  {currentUser === post.authorId && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => navigate(`/${categoryName}/game/${subCategoryId}/posts/edit/${post.id}`)}
                        className="px-3 py-1.5 text-sm text-gray-700 hover:text-gray-900"
                      >
                        수정
                      </button>
                      <button
                        onClick={deletePost}
                        className="px-3 py-1.5 text-sm text-gray-700 hover:text-red-700"
                      >
                        삭제
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {/* 본문 */}
              <div className="prose max-w-none mb-6">
                <div className="text-gray-800 leading-relaxed whitespace-pre-wrap text-base">
                  {post.content}
                </div>
              </div>

              {/* AI에게 물어보기 */}
              <div className="mb-6 rounded-xl overflow-hidden border border-gray-200 shadow-sm">
                <div className="flex items-center gap-3 px-4 py-3 bg-slate-800">
                  <div className="w-8 h-8 rounded-lg bg-slate-700 flex items-center justify-center flex-shrink-0">
                    <svg width="20" height="20" viewBox="0 0 32 32" fill="none">
                      <circle cx="16" cy="16" r="4" fill="#c7d2fe"/>
                      <circle cx="16" cy="16" r="2" fill="#818cf8"/>
                      <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2"/>
                      <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2" transform="rotate(60 16 16)"/>
                      <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2" transform="rotate(120 16 16)"/>
                      <circle cx="16" cy="6" r="1.5" fill="#a5b4fc"/>
                      <circle cx="24.7" cy="11" r="1.5" fill="#a5b4fc"/>
                      <circle cx="24.7" cy="21" r="1.5" fill="#a5b4fc"/>
                      <circle cx="16" cy="26" r="1.5" fill="#a5b4fc"/>
                      <circle cx="7.3" cy="21" r="1.5" fill="#a5b4fc"/>
                      <circle cx="7.3" cy="11" r="1.5" fill="#a5b4fc"/>
                    </svg>
                  </div>
                  <div>
                    <p className="text-white font-semibold text-sm leading-none">AI 도우미</p>
                    <p className="text-slate-400 text-xs mt-0.5">이 게시글에 대해 AI가 답변해드려요</p>
                  </div>
                  <button
                    onClick={askAi}
                    disabled={aiLoading}
                    className={`ml-auto px-4 py-1.5 rounded-lg text-sm font-medium transition-all ${
                      aiLoading
                        ? "bg-slate-600 text-slate-400 cursor-not-allowed"
                        : "bg-indigo-500 hover:bg-indigo-400 text-white"
                    }`}
                  >
                    {aiLoading ? (
                      <span className="flex items-center gap-1.5">
                        <svg className="animate-spin w-3.5 h-3.5" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                        </svg>
                        답변 생성 중...
                      </span>
                    ) : aiAnswer ? "다시 물어보기" : "AI 답변 받기"}
                  </button>
                </div>
                {aiAnswer && (
                  <div className="px-4 py-4 bg-white">
                    <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-wrap">{aiAnswer}</p>
                  </div>
                )}
                {!aiAnswer && !aiLoading && (
                  <div className="px-4 py-4 bg-white">
                    <p className="text-slate-400 text-sm">버튼을 눌러 AI 답변을 받아보세요</p>
                  </div>
                )}
              </div>

              {/* 좋아요 */}
              <div className="text-center">
                <button
                  onClick={toggleLike}
                  disabled={!currentUser}
                  className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-md text-sm font-medium transition-all ${
                    likeStatus.liked 
                      ? "bg-primary-50 text-primary-600 border-2 border-primary-600" 
                      : "bg-gray-50 text-gray-600 border-2 border-gray-300 hover:border-gray-400"
                  } ${!currentUser ? "opacity-50 cursor-not-allowed" : ""}`}
                >
                  <svg 
                    className="w-5 h-5" 
                    fill={likeStatus.liked ? "currentColor" : "none"} 
                    stroke="currentColor" 
                    strokeWidth={likeStatus.liked ? 0 : 2.5} 
                    viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                  </svg>
                  <span className="font-semibold">{likeStatus.likeCount}</span>
                </button>
                {!currentUser && (
                  <p className="text-sm text-gray-500 mt-3">로그인하시면 추천하실 수 있습니다</p>
                )}
              </div>
            </article>

            {/* 댓글 */}
            <div className="bg-white rounded-lg border border-neutral-200 p-6">
              <CommentSection 
                postId={post.id} 
                postAuthorName={post.author}  
                commentCount={post.commentCount}
                onCommentCountChange={(newCount) =>
                  setPost(prev => prev ? { ...prev, commentCount: newCount } : prev)
                }
              />
            </div>
            </main>
        </div>
      </div>
    </div>
  );
}
