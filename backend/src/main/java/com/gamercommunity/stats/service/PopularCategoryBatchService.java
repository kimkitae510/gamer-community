package com.gamercommunity.stats.service;

import com.gamercommunity.stats.repository.CategoryDailyStatsRepository;
import com.gamercommunity.stats.repository.CategoryMonthlyStatsRepository;
import com.gamercommunity.stats.repository.CategoryWeeklyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularCategoryBatchService {

    private static final int TOP_N = 7; // 상위 7개만 선정

    private final CategoryDailyStatsRepository dailyStatsRepository;
    private final CategoryWeeklyStatsRepository weeklyStatsRepository;
    private final CategoryMonthlyStatsRepository monthlyStatsRepository;

    // 일간 인기 게시판 Top 7 선정
    @Transactional
    public void updateDailyTopBoards(LocalDate date) {
        //모든 플래그 초기화
        dailyStatsRepository.resetAllTopFlags(date);
        
        //상위 7개 플래그 설정
        dailyStatsRepository.markTopN(date, TOP_N);
        
        log.info("일간 인기 게시판 Top {} 선정 완료: {}", TOP_N, date);
    }

    // 주간 인기 게시판 Top 7 선정
    @Transactional
    public void updateWeeklyTopBoards(int year, int week) {
        //모든 플래그 초기화
        weeklyStatsRepository.resetAllTopFlags(year, week);
        //상위 7개 플래그 설정
        weeklyStatsRepository.markTopN(year, week, TOP_N);
        
        log.info("주간 인기 게시판 Top {} 선정 완료: {}-W{}", TOP_N, year, week);
    }

    // 월간 인기 게시판 Top 7 선정
    @Transactional
    public void updateMonthlyTopBoards(String yearMonth) {
        //모든 플래그 초기화
        monthlyStatsRepository.resetAllTopFlags(yearMonth);
        
        //상위 7개 플래그 설정
        monthlyStatsRepository.markTopN(yearMonth, TOP_N);
        
        log.info("월간 인기 게시판 Top {} 선정 완료: {}", TOP_N, yearMonth);
    }
}
