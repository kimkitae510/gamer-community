package com.gamercommunity.stats.controller;

import com.gamercommunity.stats.dto.TopCategoryResponse;
import com.gamercommunity.stats.service.CategoryStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/top-categories")
@RequiredArgsConstructor
public class TopCategoryController {

    private final CategoryStatsService categoryStatsService;

    // 오늘 일간 Top 7 조회
    @GetMapping("/daily")
    public ResponseEntity<List<TopCategoryResponse>> getDailyTopCategories() {
        return ResponseEntity.ok(categoryStatsService.getDailyTopCategories());
    }

    // 이번 주 주간 Top 7 조회
    @GetMapping("/weekly")
    public ResponseEntity<List<TopCategoryResponse>> getWeeklyTopCategories() {
        return ResponseEntity.ok(categoryStatsService.getWeeklyTopCategories());
    }

    // 이번 달 월간 Top 7 조회
    @GetMapping("/monthly")
    public ResponseEntity<List<TopCategoryResponse>> getMonthlyTopCategories() {
        return ResponseEntity.ok(categoryStatsService.getMonthlyTopCategories());
    }
}
