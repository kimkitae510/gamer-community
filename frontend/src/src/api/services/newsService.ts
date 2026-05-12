import apiClient from '../axios';
import type { NewsItem, Platform } from '../types';

export const newsService = {
  //< 뉴스 목록 조회 (플랫폼 필터 선택 가능)
  async getNews(platform: Platform | null, limit: number = 10): Promise<NewsItem[]> {
    try {
      const params: any = { limit };
      if (platform) {
        params.platform = platform;
      }
      const response = await apiClient.get('/news', { params });
      return response.data;
    } catch (error) {
      console.error('뉴스 조회 실패:', error);
      return [];
    }
  }
};
