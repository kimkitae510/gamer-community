import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../api/axios";
import { categoryService, authService } from "../../api/services";
import { formatSmartDate } from "../../utils/dateFormat";

interface Review {
  id: number;
  content: string;
  rating: number;
  authorName: string;
  authorId: string;
  createdAt: string;
  likeCount: number;
  status: string;
}

interface ReviewWithLike extends Review {
  liked: boolean;
}

interface ReviewLikeResponse {
  id: number;
  liked: boolean;
  likeCount: number;
}

export default function GameReviewPage() {
  const { gameId } = useParams<{ gameId: string }>();
  const [reviews, setReviews] = useState<ReviewWithLike[]>([]);
  const [gameName, setGameName] = useState("");
  const [gameImageUrl, setGameImageUrl] = useState("");
  const [gameRating, setGameRating] = useState<number>(0);
  const [gameRatingCount, setGameRatingCount] = useState<number>(0);
  const [gamePostCount, setGamePostCount] = useState<number>(0);
  const [newReview, setNewReview] = useState("");
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [currentUsername, setCurrentUsername] = useState("");
  const [editId, setEditId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState("");
  const [editRating, setEditRating] = useState(0);
  const [editHoverRating, setEditHoverRating] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      
      // 게임 정보 가져오기
      try {
        const gameData = await categoryService.getChildById(Number(gameId));
        setGameName(gameData.name);
        setGameImageUrl(gameData.imageUrl);
        setGameRating(gameData.rating || 0);
        setGameRatingCount(gameData.ratingCount || 0);
        setGamePostCount(gameData.postCount || 0);
      } catch (err) {
        console.error("게임 정보 조회 실패:", err);
      }
      
      // 로그인 체크
      let loginId = "";
      try {
        const userRes = await authService.getMe();
        loginId = userRes.loginId;
        setCurrentUsername(loginId);
      } catch {
        setCurrentUsername("");
      }

      const reviewRes = await api.get(`/reviews/games/${gameId}`);
      const list: Review[] = reviewRes.data;

      const activeReviews = list.filter(r => r.status === 'ACTIVE');
      const reviewIds = activeReviews.map(r => r.id);

      let likeStatusMap: Record<number, boolean> = {};
      if (loginId && reviewIds.length > 0) {
        try {
          const likeRes = await api.get<Record<number, boolean>>(`/reviews/like-status`, {
            params: { reviewIds: reviewIds.join(',') }
          });
          likeStatusMap = likeRes.data;
        } catch {
          console.error("좋아요 상태 조회 실패");
        }
      }

      const updated: ReviewWithLike[] = activeReviews.map(r => ({
        ...r,
        liked: likeStatusMap[r.id] || false
      }));

      setReviews(updated);
    } catch (err) {
      console.error("리뷰 조회 실패:", err);
      setReviews([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!gameId) return;
    fetchReviews();
  }, [gameId]);

  const handleSubmit = async () => {
    if (!currentUsername) {
      alert("로그인이 필요합니다.");
      return;
    }
    if (!newReview.trim() || rating === 0) {
      alert("리뷰 내용과 별점을 입력해주세요.");
      return;
    }
    
    setSubmitting(true);
    try {
      const res = await api.post<Review>(`/reviews`, { gameId, content: newReview, rating });
      setReviews([{ ...res.data, liked: false }, ...reviews]);
      setNewReview("");
      setRating(0);
      setHoverRating(0);
      
      // 평점 즉시 반영
      try {
        const gameData = await categoryService.getChildById(Number(gameId));
        setGameRating(gameData.rating || 0);
        setGameRatingCount(gameData.ratingCount || 0);
      } catch {}
      
      alert("리뷰가 작성되었습니다!");
    } catch (err: any) {
      alert(err.response?.data?.message || "이미 리뷰를 작성하셨습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (r: ReviewWithLike) => {
    setEditId(r.id);
    setEditContent(r.content);
    setEditRating(r.rating);
    setEditHoverRating(0);
  };

  const handleUpdate = async (id: number) => {
    try {
      const res = await api.patch<Review>(`/reviews/${id}`, { content: editContent, rating: editRating });
      setReviews((prev) =>
        prev.map((r) =>
          r.id === id ? { ...res.data, liked: r.liked } : r
        )
      );
      setEditId(null);
      // 평점 갱신
      try {
        const gameData = await categoryService.getChildById(Number(gameId));
        setGameRating(gameData.rating || 0);
        setGameRatingCount(gameData.ratingCount || 0);
      } catch {}
      alert("수정되었습니다.");
    } catch {
      alert("수정 실패");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("삭제하시겠습니까?")) return;
    try {
      await api.delete(`/reviews/${id}`);
      setReviews((prev) => prev.filter((r) => r.id !== id));
      // 평점 갱신
      try {
        const gameData = await categoryService.getChildById(Number(gameId));
        setGameRating(gameData.rating || 0);
        setGameRatingCount(gameData.ratingCount || 0);
      } catch {}
    } catch {
      alert("삭제 실패");
    }
  };

  const toggleLike = async (reviewId: number) => {
    if (!currentUsername) {
      alert("로그인이 필요합니다.");
      return;
    }

    setReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? { ...r, liked: !r.liked, likeCount: r.liked ? r.likeCount - 1 : r.likeCount + 1 }
          : r
      )
    );

    try {
      const res = await api.post<ReviewLikeResponse>(`/reviews/${reviewId}/like`);
      setReviews((prev) =>
        prev.map((r) =>
          r.id === reviewId
            ? { ...r, liked: res.data.liked, likeCount: res.data.likeCount }
            : r
        )
      );
    } catch {
      setReviews((prev) =>
        prev.map((r) =>
          r.id === reviewId
            ? { ...r, liked: !r.liked, likeCount: r.liked ? r.likeCount + 1 : r.likeCount - 1 }
            : r
        )
      );
    }
  };

  const StarRating = ({ 
    currentRating, 
    hoverValue = 0, 
    editable = false, 
    onClick, 
    onHover, 
    onLeave,
    size = "text-sm" 
  }: { 
    currentRating: number; 
    hoverValue?: number; 
    editable?: boolean; 
    onClick?: (value: number) => void; 
    onHover?: (value: number) => void; 
    onLeave?: () => void;
    size?: string;
  }) => (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = (hoverValue || currentRating) >= star;
        return (
          <button
            key={star}
            className={`${size} transition-all ${
              filled ? "text-yellow-400" : "text-neutral-300"
            } ${editable ? "cursor-pointer hover:scale-110" : "cursor-default"}`}
            onClick={() => editable && onClick?.(star)}
            onMouseEnter={() => editable && onHover?.(star)}
            onMouseLeave={() => editable && onLeave?.()}
            disabled={!editable}
          >
            ★
          </button>
        );
      })}
    </div>
  );

  const hasUserReviewed = reviews.some((r) => r.authorName === currentUsername);

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="animate-spin w-10 h-10 border-3 border-neutral-200 border-t-primary-600 rounded-full"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* 히어로 이미지 섹션 */}
      <div className="relative h-[600px] overflow-hidden bg-black">
        {/* 배경 이미지 */}
        <div className="absolute inset-0">
          <img 
            src="/images/gammmm.JPG" 
            alt="Game"
            className="w-full h-full object-contain opacity-80"
          />
          {/* 그라데이션 오버레이 - 아래쪽만 살짝 어둡게 */}
          <div className="absolute inset-0 bg-gradient-to-b from-black/10 via-transparent to-black/70"></div>
        </div>

        {/* 컨텐츠 */}
        <div className="relative container-custom h-full flex flex-col justify-end pb-16">
          <div className="max-w-5xl">
            {/* 게임 타이틀 */}
            <h1 className="text-5xl md:text-6xl font-bold mb-8 text-white drop-shadow-2xl">
              {gameName || "게임 리뷰"}
            </h1>

            {/* 통계 카드들 */}
            <div className="flex items-center gap-4">
              {/* 평점 */}
              <div className="bg-white/95 backdrop-blur-md rounded-xl px-7 py-4 shadow-2xl border border-white/20">
                <div className="text-sm font-medium text-neutral-600 mb-1">평점</div>
                <div className="text-4xl font-black text-primary-600">{gameRating.toFixed(1)}</div>
              </div>

              {/* 리뷰 */}
              <div className="bg-white/95 backdrop-blur-md rounded-xl px-7 py-4 shadow-2xl border border-white/20">
                <div className="text-sm font-medium text-neutral-600 mb-1">리뷰</div>
                <div className="text-4xl font-black text-neutral-900">{gameRatingCount}</div>
              </div>

              {/* 게시글 */}
              <div className="bg-white/95 backdrop-blur-md rounded-xl px-7 py-4 shadow-2xl border border-white/20">
                <div className="text-sm font-medium text-neutral-600 mb-1">게시글</div>
                <div className="text-4xl font-black text-neutral-900">{gamePostCount}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container-custom py-8 md:py-12">

        {/* 리뷰 작성 */}
        {!hasUserReviewed && (
          <div className="bg-white rounded-2xl p-8 shadow-md border border-neutral-200 mb-8">
            <h2 className="text-2xl font-bold text-neutral-900 mb-6">
              리뷰 작성하기
            </h2>
            
            {!currentUsername ? (
              // 로그인 안 한 상태
              <div className="text-center py-12 bg-neutral-50 rounded-xl border-2 border-neutral-200">
                <img 
                  src="/images/animal.png" 
                  alt="로그인 필요" 
                  className="w-20 h-20 mx-auto mb-4 object-contain"
                />
                <h3 className="text-xl font-bold text-neutral-900 mb-2">로그인이 필요합니다</h3>
                <p className="text-neutral-600 mb-6">리뷰를 작성하려면 로그인해주세요</p>
                <button
                  onClick={() => window.location.href = '/login'}
                  className="px-8 py-3 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl hover:from-primary-700 hover:to-primary-800 transition-all font-semibold shadow-md hover:shadow-lg"
                >
                  로그인하러 가기
                </button>
              </div>
            ) : (
              // 로그인 한 상태
              <>
                <div className="mb-6">
                  <label className="block text-sm font-semibold text-neutral-700 mb-3">별점을 선택해주세요</label>
                  <StarRating
                    currentRating={rating}
                    hoverValue={hoverRating}
                    editable
                    onClick={setRating}
                    onHover={setHoverRating}
                    onLeave={() => setHoverRating(0)}
                    size="text-3xl"
                  />
                </div>
                <div className="mb-6">
                  <label className="block text-sm font-semibold text-neutral-700 mb-3">리뷰 내용</label>
                  <textarea
                    value={newReview}
                    onChange={(e) => setNewReview(e.target.value)}
                    className="w-full p-4 border-2 border-neutral-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
                    rows={5}
                    placeholder="게임에 대한 솔직한 리뷰를 작성해주세요..."
                  />
                </div>
                <button
                  onClick={handleSubmit}
                  disabled={submitting}
                  className="px-8 py-3 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl hover:from-primary-700 hover:to-primary-800 transition-all disabled:from-neutral-300 disabled:to-neutral-400 font-semibold shadow-md hover:shadow-lg"
                >
                  {submitting ? '작성 중...' : '리뷰 게시하기'}
                </button>
              </>
            )}
          </div>
        )}

        {/* 리뷰 목록 */}
        <div className="space-y-4">
          <h2 className="text-2xl font-bold text-neutral-900 mb-6">
            모든 리뷰 ({reviews.length})
          </h2>
          
          {reviews.length === 0 ? (
            <div className="text-center py-16 bg-white rounded-2xl border-2 border-neutral-100">
              <img src="/images/animal.png" alt="리뷰 없음" className="w-20 h-20 mx-auto mb-4 object-contain opacity-60" />
              <p className="text-neutral-500 text-lg">아직 리뷰가 없습니다</p>
              <p className="text-neutral-400 text-sm mt-2">첫 번째 리뷰를 작성해보세요!</p>
            </div>
          ) : (
            reviews.map((review) => (
              <div key={review.id} className="bg-white border-2 border-neutral-100 rounded-2xl p-6 hover:shadow-xl hover:border-primary-200 transition-all duration-200">
                {editId === review.id ? (
                  // 수정 모드
                  <div>
                    <div className="mb-3">
                      <StarRating
                        currentRating={editRating}
                        hoverValue={editHoverRating}
                        editable
                        onClick={setEditRating}
                        onHover={setEditHoverRating}
                        onLeave={() => setEditHoverRating(0)}
                        size="text-xl"
                      />
                    </div>
                    <textarea
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                      className="w-full p-4 border-2 border-neutral-200 rounded-xl mb-4 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      rows={4}
                    />
                    <div className="flex gap-3">
                      <button
                        onClick={() => handleUpdate(review.id)}
                        className="px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 font-semibold transition-all shadow-md"
                      >
                        저장
                      </button>
                      <button
                        onClick={() => setEditId(null)}
                        className="px-6 py-2.5 bg-neutral-100 text-neutral-700 rounded-xl hover:bg-neutral-200 font-semibold transition-all"
                      >
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  // 보기 모드
                  <div>
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center font-bold text-white">
                          {review.authorName?.charAt(0).toUpperCase() || '?'}
                        </div>
                        <div>
                          <div className="font-semibold text-neutral-900">{review.authorName}</div>
                          <div className="text-xs text-neutral-500">{formatSmartDate(review.createdAt)}</div>
                        </div>
                      </div>
                      
                      {(currentUsername === review.authorName || currentUsername === review.authorId) && (
                        <div className="flex gap-2">
                          <button
                            onClick={() => {
                              handleEdit(review);
                            }}
                            className="px-3 py-1.5 bg-neutral-100 text-neutral-700 text-sm font-medium rounded-md hover:bg-neutral-200 transition-colors"
                          >
                            수정
                          </button>
                          <button
                            onClick={() => handleDelete(review.id)}
                            className="px-3 py-1.5 bg-neutral-100 text-neutral-700 text-sm font-medium rounded-md hover:bg-neutral-200 transition-colors"
                          >
                            삭제
                          </button>
                        </div>
                      )}
                    </div>
                    
                    <div className="mb-4">
                      <StarRating currentRating={review.rating} size="text-xl" />
                    </div>
                    
                    <p className="text-neutral-700 mb-6 whitespace-pre-wrap leading-relaxed">{review.content}</p>
                    
                    <div className="flex items-center gap-4 pt-4 border-t border-neutral-100">
                      <button
                        onClick={() => toggleLike(review.id)}
                        disabled={!currentUsername}
                        className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-md text-sm font-medium transition-all ${
                          review.liked 
                            ? "bg-primary-50 text-primary-600 border-2 border-primary-600" 
                            : "bg-gray-50 text-gray-600 border-2 border-gray-300 hover:border-gray-400"
                        } ${!currentUsername ? "opacity-50 cursor-not-allowed" : ""}`}
                      >
                        <svg 
                          className="w-5 h-5" 
                          fill={review.liked ? "currentColor" : "none"} 
                          stroke="currentColor" 
                          strokeWidth={review.liked ? 0 : 2.5} 
                          viewBox="0 0 24 24"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                        </svg>
                        <span className="font-semibold">{review.likeCount}</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
