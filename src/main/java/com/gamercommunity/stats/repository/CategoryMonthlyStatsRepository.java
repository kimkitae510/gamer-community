package com.gamercommunity.stats.repository;

import com.gamercommunity.stats.entity.CategoryMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryMonthlyStatsRepository extends JpaRepository<CategoryMonthlyStats, Long> {

    // UPSERT: 있으면 +1, 없으면 INSERT
    @Modifying
    @Query(value = """
        INSERT INTO category_monthly_stats (category_id, year_month, post_count, is_top)
        VALUES (:categoryId, :yearMonth, 1, false)
        ON DUPLICATE KEY UPDATE post_count = post_count + 1
        """, nativeQuery = true)
    void upsertPostCount(@Param("categoryId") Long categoryId, @Param("yearMonth") String yearMonth);


    // 특정 월의 모든 isTop을 false로 초기화
    @Modifying
    @Query("UPDATE CategoryMonthlyStats s SET s.isTop = false WHERE s.yearMonth = :yearMonth")
    void resetAllTopFlags(@Param("yearMonth") String yearMonth);

    // 특정 월의 상위 N개를 isTop = true로 설정
    @Modifying
    @Query(value = """
        UPDATE category_monthly_stats 
        SET is_top = true 
        WHERE year_month = :yearMonth 
        ORDER BY post_count DESC 
        LIMIT :limit
        """, nativeQuery = true)
    void markTopN(@Param("yearMonth") String yearMonth, @Param("limit") int limit);

    // isTop = true인 월간 인기 게시판만 조회
    @Query("SELECT s FROM CategoryMonthlyStats s JOIN FETCH s.category WHERE s.yearMonth = :yearMonth AND s.isTop = true ORDER BY s.postCount DESC")
    List<CategoryMonthlyStats> findTopBoardsByYearMonth(@Param("yearMonth") String yearMonth);
}
