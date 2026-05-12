package com.gamercommunity.ai.service;

import com.gamercommunity.ai.usage.AiUsageService;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommentService {

    private final PostRepository postRepository;
    private final LlmService llmService;
    private final AiCommentWriter aiCommentWriter;
    private final AiUsageService aiUsageService;


    //ai 댓글 요청 (비동기)
    @Async("llmExecutor")
    public void generateAiCommentAsync(Long taskId, Long postId, Long userId) {
        try {
            // 게시글 조회
            Post post = postRepository.findByIdWithDetails(postId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글", postId));
            String categoryName = post.getCategory() != null ? post.getCategory().getName() : "게임";

            // LLM 호출 (동기 블로킹이지만 비동기 스레드이므로 톰캣 무관)
            log.info("[AiCommentService] LLM 호출 시작 - taskId={}, postId={}", taskId, postId);
            String aiAnswer = llmService.generateAnswer(categoryName, post.getTitle(), post.getContent());
            log.info("[AiCommentService] LLM 호출 완료 - taskId={}, postId={}", taskId, postId);

            // 댓글 저장 + COMPLETED
            aiCommentWriter.saveCommentAndComplete(taskId, postId, aiAnswer);

            // 쿼터 차감
            aiUsageService.decreaseQuota(userId);

        } catch (Exception e) {
            log.error("[AiCommentService] AI 댓글 생성 실패 - taskId={}, postId={}", taskId, postId, e);
            aiCommentWriter.markFailed(taskId);
        } finally {
            // 락 해제 (성공/실패 무관)
            aiUsageService.unlockUser(userId);
        }
    }
}
