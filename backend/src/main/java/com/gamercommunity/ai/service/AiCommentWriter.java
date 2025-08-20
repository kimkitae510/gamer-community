package com.gamercommunity.ai.service;

import com.gamercommunity.ai.entity.AiComment;
import com.gamercommunity.ai.repository.AiCommentRepository;
import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// AI 댓글 생성 과정의 DB 쓰기 담당
// 각 메서드는 짧은 트랜잭션만 점유
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommentWriter {

    private static final String AI_BOT_LOGIN_ID = "rlarlxo51000";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AiCommentRepository aiCommentRepository;

    // PENDING 상태의 AiComment 레코드 생성 (톰캣 스레드에서 호출)
    @Transactional
    public Long createPendingRecord(Long postId, Long userId) {
        AiComment aiComment = AiComment.builder()
                .postId(postId)
                .userId(userId)
                .build();
        Long taskId = aiCommentRepository.save(aiComment).getId();
        log.info("[AiCommentWriter] PENDING 생성 - taskId={}, postId={}, userId={}", taskId, postId, userId);
        return taskId;
    }

    // AI 봇 댓글 저장 + AiComment COMPLETED 처리
    @Transactional
    public void saveCommentAndComplete(Long taskId, Long postId, String answer) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));
        User aiBotUser = userRepository.findByLoginId(AI_BOT_LOGIN_ID)
                .orElseThrow(() -> new IllegalStateException("AI 도우미 계정을 찾을 수 없습니다."));

        commentRepository.save(Comment.builder()
                .content(answer)
                .post(post)
                .author(aiBotUser)
                .parent(null)
                .build());
        postRepository.incrementCommentCount(postId);

        AiComment aiComment = aiCommentRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("AI 작업 레코드를 찾을 수 없습니다. taskId=" + taskId));
        aiComment.complete(answer);

        log.info("[AiCommentWriter] 댓글 저장 + COMPLETED - taskId={}, postId={}", taskId, postId);
    }

    // AiComment FAILED 처리
    @Transactional
    public void markFailed(Long taskId) {
        try {
            aiCommentRepository.findById(taskId).ifPresent(AiComment::fail);
            log.warn("[AiCommentWriter] FAILED 처리 - taskId={}", taskId);
        } catch (Exception e) {
            log.error("[AiCommentWriter] FAILED 처리 중 오류 - taskId={}", taskId, e);
        }
    }
}
