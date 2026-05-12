import apiClient from '../axios';

export interface TopCategory {
  categoryId: number;
  categoryName: string;
  postCount: number;
  rank: number;
  rating: number;
  ratingCount: number;
  imageUrl: string;
  genres: string[];
}

export const topCategoryService = {
  //< 일간 Top 7
  getDailyTop: async (): Promise<TopCategory[]> => {
    const response = await apiClient.get('/top-categories/daily');
    return response.data;
  },

  //< 주간 Top 7
  getWeeklyTop: async (): Promise<TopCategory[]> => {
    const response = await apiClient.get('/top-categories/weekly');
    return response.data;
  },

  //< 월간 Top 7
  getMonthlyTop: async (): Promise<TopCategory[]> => {
    const response = await apiClient.get('/top-categories/monthly');
    return response.data;
  },
};