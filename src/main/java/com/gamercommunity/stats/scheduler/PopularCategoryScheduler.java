package com.gamercommunity.stats.scheduler;

import com.gamercommunity.stats.service.PopularCategoryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularCategoryScheduler {

    private final PopularCategoryBatchService batchService;

    // 일간 인기 게시판 선정 - 매일 12:00에 실행
    @Scheduled(cron = "0 0 12 * * *")
    public void updateDailyTopBoards() {
        LocalDate today = LocalDate.now();
        log.info("=== 일간 인기 게시판 배치 시작: {} ===", today);
        
        try {
            batchService.updateDailyTopBoards(today);
            log.info("=== 일간 인기 게시판 배치 완료 ===");
        } catch (Exception e) {
            log.error("일간 인기 게시판 배치 실패: {}", e.getMessage(), e);
        }
    }

    // 주간 인기 게시판 선정 - 매주 월요일 12:00에 실행
    @Scheduled(cron = "0 0 12 * * MON")
    public void updateWeeklyTopBoards() {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = today.getYear();
        int week = today.get(weekFields.weekOfWeekBasedYear());
        
        log.info("=== 주간 인기 게시판 배치 시작: {}-W{} ===", year, week);
        
        try {
            batchService.updateWeeklyTopBoards(year, week);
            log.info("=== 주간 인기 게시판 배치 완료 ===");
        } catch (Exception e) {
            log.error("주간 인기 게시판 배치 실패: {}", e.getMessage(), e);
        }
    }

    // 월간 인기 게시판 선정 - 매달 1일 12:00에 실행
    @Scheduled(cron = "0 0 12 1 * *")
    public void updateMonthlyTopBoards() {
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        log.info("=== 월간 인기 게시판 배치 시작: {} ===", yearMonth);
        
        try {
            batchService.updateMonthlyTopBoards(yearMonth);
            log.info("=== 월간 인기 게시판 배치 완료 ===");
        } catch (Exception e) {
            log.error("월간 인기 게시판 배치 실패: {}", e.getMessage(), e);
        }
    }
}
