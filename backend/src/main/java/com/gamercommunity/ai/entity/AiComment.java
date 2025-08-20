package com.gamercommunity.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 댓글 생성 작업 추적 엔티티.
 *
 * PENDING   → 비동기 LLM 호출 시작, 프론트에서 폴링 중
 * COMPLETED → LLM 응답 수신 + 댓글 저장 완료
 * FAILED    → LLM 호출 또는 후속 처리 실패
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ai_comment")
public class AiComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiCommentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Builder
    public AiComment(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
        this.status = AiCommentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void complete(String aiAnswer) {
        validateStatus(AiCommentStatus.PENDING);
        this.content = aiAnswer;
        this.status = AiCommentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        if (this.status == AiCommentStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 AI 댓글은 실패 처리할 수 없습니다.");
        }
        this.status = AiCommentStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == AiCommentStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.status == AiCommentStatus.COMPLETED;
    }

    private void validateStatus(AiCommentStatus required) {
        if (this.status != required) {
            throw new IllegalStateException(
                    String.format("상태 전이 불가: 현재=%s, 필요=%s", this.status, required));
        }
    }
}
