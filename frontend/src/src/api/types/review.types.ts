// 리뷰 관련 타입 정의
export interface Review {
  id: number;
  content: string;
  rating: number;
  author: string;
  authorId: string;
  gameId: number;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  isLiked?: boolean;
}

export interface ReviewRequest {
  content: string;
  rating: number;
  gameId: number;
}
