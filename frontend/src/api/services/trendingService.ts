import api from '../axios';
import type { TrendingPost } from '../types/trending.types';

export const trendingService = {
  //< 인기글 전체 조회
  getAllTrending: async (): Promise<TrendingPost[]> => {
    const response = await api.get('/popular/trending');
    return response.data;
  },

  //< 인기글 Top 10 조회 (사이드바용)
  getTop10Trending: async (): Promise<TrendingPost[]> => {
    const response = await api.get('/popular/trending/top10');
    return response.data;
  },
};
