import { useEffect, useState } from "react";
import { trendingService, topCategoryService } from "../api/services";
import type { TopCategory } from "../api/types";
import type { TrendingPost } from "../api/types/trending.types";

/**
 * 사이드바 데이터(인기글 + 인기 게시판)를 관리하는 공통 훅
 * - PostListPage, PostDetailPage, CategoryBoardPage에서 중복되던 로직 통합
 */
export function useSidebarData() {
  const [trendingPosts, setTrendingPosts] = useState<TrendingPost[]>([]);
  const [topCategories, setTopCategories] = useState<TopCategory[]>([]);
  const [selectedPeriod, setSelectedPeriod] = useState<'daily' | 'weekly' | 'monthly'>('daily');

  useEffect(() => {
    trendingService.getTop10Trending().then(setTrendingPosts).catch(console.error);
    topCategoryService.getDailyTop().then(setTopCategories).catch(console.error);
  }, []);

  const handlePeriodChange = async (period: 'daily' | 'weekly' | 'monthly') => {
    setSelectedPeriod(period);
    try {
      const periodMap = {
        daily: topCategoryService.getDailyTop,
        weekly: topCategoryService.getWeeklyTop,
        monthly: topCategoryService.getMonthlyTop,
      };
      setTopCategories(await periodMap[period]());
    } catch (error) {
      console.error(error);
    }
  };

  return {
    trendingPosts,
    topCategories,
    selectedPeriod,
    handlePeriodChange,
  };
}
