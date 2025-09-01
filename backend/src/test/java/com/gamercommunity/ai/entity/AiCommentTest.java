package com.gamercommunity.ai.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AiCommentTest {

    @Test
    @DisplayName("AiComment 생성 시 PENDING 상태")
    void create_pending() {
        AiComment aiComment = AiComment.builder()
                .postId(1L)
                .userId(1L)
                .build();

        assertThat(aiComment.getStatus()).isEqualTo(AiCommentStatus.PENDING);
        assertThat(aiComment.isPending()).isTrue();
        assertThat(aiComment.isCompleted()).isFalse();
        assertThat(aiComment.getContent()).isNull();
    }

    @Test
    @DisplayName("PENDING → COMPLETED 상태 전이")
    void complete_success() {
        AiComment aiComment = AiComment.builder()
                .postId(1L)
                .userId(1L)
                .build();

        aiComment.complete("AI 답변입니다.");

        assertThat(aiComment.getStatus()).isEqualTo(AiCommentStatus.COMPLETED);
        assertThat(aiComment.isCompleted()).isTrue();
        assertThat(aiComment.getContent()).isEqualTo("AI 답변입니다.");
        assertThat(aiComment.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("COMPLETED 상태에서 complete 호출 시 예외")
    void complete_alreadyCompleted() {
        AiComment aiComment = AiComment.builder()
                .postId(1L)
                .userId(1L)
                .build();

        aiComment.complete("답변");

        assertThatThrownBy(() -> aiComment.complete("또 답변"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상태 전이 불가");
    }

    @Test
    @DisplayName("PENDING → FAILED 상태 전이")
    void fail_success() {
        AiComment aiComment = AiComment.builder()
                .postId(1L)
                .userId(1L)
                .build();

        aiComment.fail();

        assertThat(aiComment.getStatus()).isEqualTo(AiCommentStatus.FAILED);
    }

    @Test
    @DisplayName("COMPLETED 상태에서 fail 호출 시 예외")
    void fail_afterComplete() {
        AiComment aiComment = AiComment.builder()
                .postId(1L)
                .userId(1L)
                .build();

        aiComment.complete("답변");

        assertThatThrownBy(() -> aiComment.fail())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 완료된");
    }
}
