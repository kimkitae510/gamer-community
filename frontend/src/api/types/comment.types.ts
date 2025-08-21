// 댓글 관련 타입 정의
export interface Comment {
  id: number;
  content: string;
  author: string;          // 백엔드: authorName
  authorId: string;        // 백엔드: authorId
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  parentId: number | null;
  postId: number;
  isLiked?: boolean;       // 프론트엔드에서 추가
  replies?: Comment[];     // 백엔드: children
}

export interface CommentRequest {
  content: string;
  postId: number;
  parentId?: number | null;
}

// 백엔드 응답 타입 (실제 API 응답)
export interface CommentResponse {
  id: number;
  content: string;
  authorName: string;      // 백엔드 필드명
  authorId: string;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  parentId: number | null;
  postId: number;
  children?: CommentResponse[];  // 백엔드 필드명
}
