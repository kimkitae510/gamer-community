package com.gamercommunity.ai.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

    Optional<AiUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    // remaining > 0 조건부 원자적 차감
    // 반환값: 업데이트된 row 수 (0 = 잔여 없음, 1 = 차감 성공)
    @Modifying
    @Query("""
        UPDATE AiUsage a
        SET a.remaining = a.remaining - 1
        WHERE a.user.id = :userId
          AND a.usageDate = :today
          AND a.remaining > 0
        """)
    int decrementIfRemaining(@Param("userId") Long userId,
                             @Param("today") LocalDate today);

    // DB 기반 락 획득 (원자적 UPDATE)
    // 조건: isLocked = false 이거나, lastRequestAt이 30초 이상 경과 (타임아웃 복구)
    // 반환값: 0 = 락 획득 실패, 1 = 성공
    @Modifying
    @Query(value = """
        UPDATE ai_usage
        SET is_locked = TRUE,
            last_request_at = NOW()
        WHERE user_id = :userId
          AND (is_locked = FALSE
               OR last_request_at < NOW() - INTERVAL 30 SECOND)
        """, nativeQuery = true)
    int tryLockUser(@Param("userId") Long userId);

    // 락 해제
    @Modifying
    @Query("""
        UPDATE AiUsage a
        SET a.isLocked = false
        WHERE a.user.id = :userId
        """)
    void unlockUser(@Param("userId") Long userId);
}
