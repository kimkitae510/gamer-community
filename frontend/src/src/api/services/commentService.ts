import api from '../axios';
import type { Comment, CommentRequest } from '../types';

//< 서버 응답을 Comment 타입으로 매핑 (필드명 차이 대응)
const mapCommentResponse = (response: any): Comment => ({
  id: response.id,
  content: response.content,
  author: response.author || response.authorName,
  authorId: response.authorId,
  createdAt: response.createdAt,
  updatedAt: response.updatedAt,
  likeCount: response.likeCount,
  parentId: response.parentId,
  postId: response.postId,
  replies: response.replies?.map(mapCommentResponse) || response.children?.map(mapCommentResponse),
});

export const commentService = {
  //< 댓글 작성
  create: async (data: CommentRequest): Promise<number> => {
    const response = await api.post<number>('/comments', data);
    return response.data;
  },

  //< 댓글 삭제
  delete: async (commentId: number): Promise<void> => {
    await api.delete(`/comments/${commentId}`);
  },

  //< 댓글 수정
  update: async (commentId: number, content: string): Promise<void> => {
    await api.patch(`/comments/${commentId}`, { content });
  },

  //< 게시글별 댓글 목록 조회
  getByPost: async (postId: number): Promise<Comment[]> => {
    const response = await api.get<any[]>(`/comments/posts/${postId}`);
    return response.data.map(mapCommentResponse);
  },
};
