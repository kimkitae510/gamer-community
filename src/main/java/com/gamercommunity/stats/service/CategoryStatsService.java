package com.gamercommunity.stats.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryGenreRepository;
import com.gamercommunity.stats.repository.CategoryDailyStatsRepository;
import com.gamercommunity.stats.repository.CategoryMonthlyStatsRepository;
import com.gamercommunity.stats.repository.CategoryWeeklyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryStatsService {

    private final CategoryDailyStatsRepository dailyStatsRepository;
    private final CategoryWeeklyStatsRepository weeklyStatsRepository;
    private final CategoryMonthlyStatsRepository monthlyStatsRepository;
    private final CategoryGenreRepository categoryGenreRepository;

    // 게시글 생성 시 호출 - 일간/주간/월간 통계 업데이트
    @Transactional
    public void onPostCreated(Category category) {
        LocalDate today = LocalDate.now();
        
        // 일간 통계 업데이트
        updateDailyStats(category, today);
        
        // 주간 통계 업데이트
        updateWeeklyStats(category, today);
        
        // 월간 통계 업데이트
        updateMonthlyStats(category, today);
    }

    // 일간 통계 업데이트
    private void updateDailyStats(Category category, LocalDate date) {
        dailyStatsRepository.upsertPostCount(category.getId(), date);
    }

    // 주간 통계 업데이트
    private void updateWeeklyStats(Category category, LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());
        
        weeklyStatsRepository.upsertPostCount(category.getId(), year, week);
    }

    // 월간 통계 업데이트
    private void updateMonthlyStats(Category category, LocalDate date) {
        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        monthlyStatsRepository.upsertPostCount(category.getId(), yearMonth);
    }

    // 오늘 일간 Top 7 조회 (isTop = true만)
    @Transactional(readOnly = true)
    public java.util.List<com.gamercommunity.stats.dto.TopCategoryResponse> getDailyTopCategories() {
        java.util.List<com.gamercommunity.stats.entity.CategoryDailyStats> stats = 
            dailyStatsRepository.findTopBoardsByDate(LocalDate.now());
        
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        return stats.stream()
                .map(s -> {
                    java.util.List<String> genreNames = categoryGenreRepository
                            .findGenresByCategoryId(s.getCategory().getId())
                            .stream()
                            .map(g -> g.getName())
                            .collect(java.util.stream.Collectors.toList());
                    
                    return com.gamercommunity.stats.dto.TopCategoryResponse.builder()
                            .categoryId(s.getCategory().getId())
                            .categoryName(s.getCategory().getName())
                            .postCount(s.getPostCount())
                            .rank(rank.getAndIncrement())
                            .rating(s.getCategory().getRating())
                            .imageUrl(s.getCategory().getImageUrl())
                            .genres(genreNames)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // 이번 주 주간 Top 7 조회 (isTop = true만)
    @Transactional(readOnly = true)
    public java.util.List<com.gamercommunity.stats.dto.TopCategoryResponse> getWeeklyTopCategories() {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = today.getYear();
        int week = today.get(weekFields.weekOfWeekBasedYear());
        
        java.util.List<com.gamercommunity.stats.entity.CategoryWeeklyStats> stats = 
            weeklyStatsRepository.findTopBoardsByYearAndWeek(year, week);
        
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        return stats.stream()
                .map(s -> {
                    java.util.List<String> genreNames = categoryGenreRepository
                            .findGenresByCategoryId(s.getCategory().getId())
                            .stream()
                            .map(g -> g.getName())
                            .collect(java.util.stream.Collectors.toList());
                    
                    return com.gamercommunity.stats.dto.TopCategoryResponse.builder()
                            .categoryId(s.getCategory().getId())
                            .categoryName(s.getCategory().getName())
                            .postCount(s.getPostCount())
                            .rank(rank.getAndIncrement())
                            .rating(s.getCategory().getRating())
                            .imageUrl(s.getCategory().getImageUrl())
                            .genres(genreNames)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // 이번 달 월간 Top 7 조회 (isTop = true만)
    @Transactional(readOnly = true)
    public java.util.List<com.gamercommunity.stats.dto.TopCategoryResponse> getMonthlyTopCategories() {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        java.util.List<com.gamercommunity.stats.entity.CategoryMonthlyStats> stats = 
            monthlyStatsRepository.findTopBoardsByYearMonth(yearMonth);
        
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        return stats.stream()
                .map(s -> {
                    java.util.List<String> genreNames = categoryGenreRepository
                            .findGenresByCategoryId(s.getCategory().getId())
                            .stream()
                            .map(g -> g.getName())
                            .collect(java.util.stream.Collectors.toList());
                    
                    return com.gamercommunity.stats.dto.TopCategoryResponse.builder()
                            .categoryId(s.getCategory().getId())
                            .categoryName(s.getCategory().getName())
                            .postCount(s.getPostCount())
                            .rank(rank.getAndIncrement())
                            .rating(s.getCategory().getRating())
                            .imageUrl(s.getCategory().getImageUrl())
                            .genres(genreNames)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
