import api from '../axios';
import type { LikeResponse } from '../types';

export const likeService = {
  //< 게시글 좋아요 토글
  togglePostLike: async (postId: number): Promise<LikeResponse> => {
    const response = await api.post<LikeResponse>(`/posts/${postId}/like`);
    return response.data;
  },

  //< 게시글 좋아요 상태 조회
  getPostLikeStatus: async (postId: number): Promise<LikeResponse> => {
    const response = await api.get<LikeResponse>(`/posts/${postId}/like-status`);
    return response.data;
  },

  //< 댓글 좋아요 토글
  toggleCommentLike: async (commentId: number): Promise<LikeResponse> => {
    const response = await api.post<LikeResponse>(`/comments/${commentId}/like`);
    return response.data;
  },

  //< 댓글 좋아요 상태 조회
  getCommentLikeStatus: async (commentId: number): Promise<LikeResponse> => {
    const response = await api.get<LikeResponse>(`/comments/${commentId}/like-status`);
    return response.data;
  },

  //< 리뷰 좋아요 토글
  toggleReviewLike: async (reviewId: number): Promise<LikeResponse> => {
    const response = await api.post<LikeResponse>(`/reviews/${reviewId}/like`);
    return response.data;
  },

  //< 리뷰 좋아요 상태 조회
  getReviewLikeStatus: async (reviewId: number): Promise<LikeResponse> => {
    const response = await api.get<LikeResponse>(`/reviews/${reviewId}/like-status`);
    return response.data;
  },
};
