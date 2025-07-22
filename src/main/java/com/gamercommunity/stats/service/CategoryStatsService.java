package com.gamercommunity.stats.service;

import com.gamercommunity.category.entity.Category;
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


    //게시글 생성 시 호출 - 일간/주간/월간 통계 업데이트
    @Transactional
    public void onPostCreated(Category category) {
        LocalDate today = LocalDate.now();

        // 일간 통계 업데이트 (UPSERT: 동시성 안전)
        updateDailyStats(category, today);

        // 주간 통계 업데이트 (UPSERT: 동시성 안전)
        updateWeeklyStats(category, today);

        // 월간 통계 업데이트 (UPSERT: 동시성 안전)
        updateMonthlyStats(category, today);
    }

    //일간 통계 업데이트
    private void updateDailyStats(Category category, LocalDate date) {
        dailyStatsRepository.upsertPostCount(category.getId(), date);
    }


    //주간 통계 업데이트
    private void updateWeeklyStats(Category category, LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());

        weeklyStatsRepository.upsertPostCount(category.getId(), year, week);
    }

    //월간 통계 업데이트 (UPSERT)
    private void updateMonthlyStats(Category category, LocalDate date) {
        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        monthlyStatsRepository.upsertPostCount(category.getId(), yearMonth);
    }
}