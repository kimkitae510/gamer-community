// 게시글 관련 타입 정의
export interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  authorId: string;
  views: number;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  tag: PostTag;
  categoryId: number;
}

export interface PostRequest {
  title: string;
  content: string;
  categoryId: number;
  tag: PostTag;
}

export type PostTag = "일반" | "질문" | "정보" | "공략";

export type PostSort = "LATEST" | "LIKED" | "VIEWED" | "COMMENTED";

export interface PostListParams {
  categoryId: number;
  sort?: PostSort;
  tag?: PostTag | null;
}
