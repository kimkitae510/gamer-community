package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryMonthlyStatsRepository extends JpaRepository<CategoryMonthlyStats, Long> {

    // 특정 카테고리의 특정 월 통계 조회
    @Query("SELECT s FROM CategoryMonthlyStats s WHERE s.category.id = :categoryId AND s.yearMonth = :yearMonth")
    Optional<CategoryMonthlyStats> findByCategoryIdAndYearMonth(@Param("categoryId") Long categoryId, @Param("yearMonth") String yearMonth);

    // 통계 row가 존재하지 않는 경우 생성
    @Modifying
    @Query(value = """
        INSERT INTO category_monthly_stats (category_id, year_month, post_count)
        VALUES (:categoryId, :yearMonth, 1)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("yearMonth") String yearMonth);
}
