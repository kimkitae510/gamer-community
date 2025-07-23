package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryWeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryWeeklyStatsRepository extends JpaRepository<CategoryWeeklyStats, Long> {


    // 게시판에 이번주 게시글 카운트 점수 있으면 +1, 없으면 INSERT
    @Modifying
    @Query(value = """
        INSERT INTO category_weekly_stats (category_id, year, week, post_count, is_top)
        VALUES (:categoryId, :year, :week, 1, false)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("year") Integer year, @Param("week") Integer week);

    // 특정 주차의 모든 isTop을 false로 초기화
    @Modifying
    @Query("UPDATE CategoryWeeklyStats s SET s.isTop = false WHERE s.year = :year AND s.week = :week")
    void resetAllTopFlags(@Param("year") Integer year, @Param("week") Integer week);

    // 특정 주차의 상위 N개를 isTop = true로 설정
    @Modifying
    @Query(value = """
        UPDATE category_weekly_stats 
        SET is_top = true 
        WHERE year = :year AND week = :week 
        ORDER BY post_count DESC 
        LIMIT :limit
        """, nativeQuery = true)
    void markTopN(@Param("year") Integer year, @Param("week") Integer week, @Param("limit") int limit);

    // isTop = true인 주간 인기 게시판만 조회
    @Query("SELECT s FROM CategoryWeeklyStats s JOIN FETCH s.category WHERE s.year = :year AND s.week = :week AND s.isTop = true ORDER BY s.postCount DESC")
    List<CategoryWeeklyStats> findTopBoardsByYearAndWeek(@Param("year") Integer year, @Param("week") Integer week);
}
