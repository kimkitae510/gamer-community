package com.gamercommunity.ai.usage;

import com.gamercommunity.global.exception.custom.AiUsageLimitExceededException;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiUsageService {

    private static final int DAILY_LIMIT = 3;

    private final AiUsageRepository aiUsageRepository;
    private final UserRepository userRepository;

    // 락 획득 시도 (원자적 UPDATE)
    // true = 획득 성공, false = 이미 진행 중
    @Transactional
    public boolean tryLockUser(Long userId) {
        ensureUsageRecordExists(userId);
        return aiUsageRepository.tryLockUser(userId) > 0;
    }

    // 락 해제 — 비동기 스레드의 finally에서 반드시 호출
    @Transactional
    public void unlockUser(Long userId) {
        aiUsageRepository.unlockUser(userId);
        log.info("[AiUsageService] 락 해제 - userId={}", userId);
    }

    // 쿼터 사전 체크 — LLM 호출 전에 remaining이 0이면 즉시 예외
    @Transactional
    public void checkQuota(Long userId) {
        ensureUsageRecordExists(userId);
        int remaining = getRemainingCount(userId);
        if (remaining <= 0) {
            throw new AiUsageLimitExceededException(
                    "오늘의 AI 질문 횟수(" + DAILY_LIMIT + "회)를 모두 사용하셨습니다.");
        }
    }

    // 쿼터 차감 — LLM 호출 성공 후 비동기 스레드에서 호출
    @Transactional
    public void decreaseQuota(Long userId) {
        LocalDate today = LocalDate.now();
        int updated = aiUsageRepository.decrementIfRemaining(userId, today);
        if (updated == 0) {
            throw new AiUsageLimitExceededException(
                    "오늘의 AI 질문 횟수(" + DAILY_LIMIT + "회)를 모두 사용하셨습니다.");
        }
        log.info("[AiUsageService] 쿼터 차감 완료 - userId={}", userId);
    }

    // loginId 기반 남은 횟수 조회
    public int getRemainingCount(String loginId) {
        return userRepository.findByLoginId(loginId)
                .map(user -> getRemainingCount(user.getId()))
                .orElse(0);
    }

    // userId 기반 남은 횟수 조회
    public int getRemainingCount(Long userId) {
        return aiUsageRepository.findByUserIdAndUsageDate(userId, LocalDate.now())
                .map(AiUsage::getRemaining)
                .orElse(DAILY_LIMIT);
    }

    // 오늘 날짜 AiUsage 레코드가 없으면 생성
    private void ensureUsageRecordExists(Long userId) {
        LocalDate today = LocalDate.now();
        if (aiUsageRepository.findByUserIdAndUsageDate(userId, today).isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            aiUsageRepository.save(AiUsage.builder()
                    .user(user)
                    .usageDate(today)
                    .remaining(DAILY_LIMIT)
                    .isLocked(false)
                    .build());
        }
    }
}
