import api from '../axios';
import type { Review, ReviewRequest } from '../types';

export const reviewService = {
  //< 리뷰 작성
  create: async (data: ReviewRequest): Promise<Review> => {
    const response = await api.post<Review>('/reviews', data);
    return response.data;
  },

  //< 리뷰 삭제
  delete: async (reviewId: number): Promise<string> => {
    const response = await api.delete<string>(`/reviews/${reviewId}`);
    return response.data;
  },

  //< 리뷰 수정
  update: async (reviewId: number, data: ReviewRequest): Promise<Review> => {
    const response = await api.put<Review>(`/reviews/${reviewId}`, data);
    return response.data;
  },

  //< 게임별 리뷰 목록 조회
  getByGame: async (gameId: number): Promise<Review[]> => {
    const response = await api.get<Review[]>(`/reviews/game/${gameId}`);
    return response.data;
  },
};
