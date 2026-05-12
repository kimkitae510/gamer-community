// 실시간 인기글 관련 타입 정의
export interface TrendingPost {
  postId: number;
  title: string;
  author: string;
  authorId: string;
  categoryId: number;
  categoryName: string;
  views: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  tag: string;
  popularScore: number;
  commentScore: number;
  likeScore: number;
  viewScore: number;
}
