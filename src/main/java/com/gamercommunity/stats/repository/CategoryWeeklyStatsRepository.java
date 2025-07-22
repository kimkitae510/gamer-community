package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryWeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryWeeklyStatsRepository extends JpaRepository<CategoryWeeklyStats, Long> {


    // 통계 row가 존재하지 않는 경우 생성
    @Modifying
    @Query(value = """
        INSERT INTO category_weekly_stats (category_id, year, week, post_count)
        VALUES (:categoryId, :year, :week, 1)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("year") Integer year, @Param("week") Integer week);

}
