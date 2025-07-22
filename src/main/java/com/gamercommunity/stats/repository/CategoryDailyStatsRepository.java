package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CategoryDailyStatsRepository extends JpaRepository<CategoryDailyStats, Long> {

    // 특정 카테고리의 특정 날짜 통계 조회
    @Query("SELECT s FROM CategoryDailyStats s WHERE s.category.id = :categoryId AND s.date = :date")
    Optional<CategoryDailyStats> findByCategoryIdAndDate(@Param("categoryId") Long categoryId, @Param("date") LocalDate date);

    // 통계 row가 존재하지 않는 경우 생성
    @Modifying
    @Query(value = """
        INSERT INTO category_daily_stats (category_id, date, post_count)
        VALUES (:categoryId, :date, 1)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("date") LocalDate date);
}
