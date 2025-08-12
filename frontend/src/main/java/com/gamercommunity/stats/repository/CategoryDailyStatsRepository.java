package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CategoryDailyStatsRepository extends JpaRepository<CategoryDailyStats, Long> {

    // 특정 카테고리의 특정 날짜 통계 조회
    @Query("SELECT s FROM CategoryDailyStats s WHERE s.category.id = :categoryId AND s.date = :date")
    Optional<CategoryDailyStats> findByCategoryIdAndDate(@Param("categoryId") Long categoryId, @Param("date") LocalDate date);

    // 게시판에 오늘 게시글 카운트 점수 있으면 +1, 없으면 INSERT
    @Modifying
    @Query(value = """
        INSERT INTO category_daily_stats (category_id, date, post_count, is_top)
        VALUES (:categoryId, :date, 1, false)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("date") LocalDate date);

    // 특정 날짜의 모든 isTop을 false로 초기화
    @Modifying
    @Query("UPDATE CategoryDailyStats s SET s.isTop = false WHERE s.date = :date")
    void resetAllTopFlags(@Param("date") LocalDate date);

    // 특정 날짜의 상위 N개를 isTop = true로 설정
    @Modifying
    @Query(value = """
        UPDATE category_daily_stats 
        SET is_top = true 
        WHERE date = :date 
        ORDER BY post_count DESC 
        LIMIT :limit
        """, nativeQuery = true)
    void markTopN(@Param("date") LocalDate date, @Param("limit") int limit);

    // isTop = true인 일간 인기 게시판만 조회
    @Query("SELECT s FROM CategoryDailyStats s JOIN FETCH s.category WHERE s.date = :date AND s.isTop = true ORDER BY s.postCount DESC")
    List<CategoryDailyStats> findTopBoardsByDate(@Param("date") LocalDate date);
}
