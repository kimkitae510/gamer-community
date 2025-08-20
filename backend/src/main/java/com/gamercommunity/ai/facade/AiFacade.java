package com.gamercommunity.ai.facade;

import com.gamercommunity.ai.dto.AiAskResponse;
import com.gamercommunity.ai.dto.AiPollResponse;
import com.gamercommunity.ai.entity.AiComment;
import com.gamercommunity.ai.repository.AiCommentRepository;
import com.gamercommunity.ai.service.AiCommentService;
import com.gamercommunity.ai.service.AiCommentWriter;
import com.gamercommunity.ai.usage.AiUsageService;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// AI 댓글 생성의 진입점
//
// [톰캣 스레드] 쿼터 확인 → 락 획득 → PENDING 생성 → 비동기 위임 → taskId 반환
// [비동기 스레드] LLM 호출 → 댓글 저장 + COMPLETED → 쿼터 차감 → 락 해제
@Slf4j
@Component
@RequiredArgsConstructor
public class AiFacade {

    private final UserRepository userRepository;
    private final AiCommentRepository aiCommentRepository;
    private final AiCommentService aiCommentService;
    private final AiCommentWriter aiCommentWriter;
    private final AiUsageService aiUsageService;

    // AI 질문 요청 → taskId 반환 (프론트가 이 ID로 폴링)
    public AiAskResponse ask(Long postId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));
        Long userId = user.getId();

        // 쿼터 사전 체크
        aiUsageService.checkQuota(userId);

        // DB 락 획득 (중복 요청 방지)
        boolean locked = aiUsageService.tryLockUser(userId);
        if (!locked) {
            throw new IllegalStateException("이미 AI 댓글 생성이 진행 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // PENDING 레코드 생성
            Long taskId = aiCommentWriter.createPendingRecord(postId, userId);

            // 비동기 LLM 호출 위임
            aiCommentService.generateAiCommentAsync(taskId, postId, userId);

            log.info("[AiFacade] AI 요청 수락 - taskId={}, postId={}, userId={}", taskId, postId, userId);

            return AiAskResponse.builder()
                    .taskId(taskId)
                    .message("AI 답변을 생성 중입니다.")
                    .build();

        } catch (Exception e) {
            log.error("[AiFacade] 비동기 진입 전 실패 - postId={}, userId={}", postId, userId, e);
            aiUsageService.unlockUser(userId);
            throw e;
        }
    }

    // 폴링: taskId로 진행 상태 조회
    public AiPollResponse poll(Long taskId) {
        AiComment aiComment = aiCommentRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("AI 작업", taskId));
        return AiPollResponse.from(aiComment);
    }
}
