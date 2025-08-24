package com.gamercommunity.ai.facade;

import com.gamercommunity.ai.dto.AiAskResponse;
import com.gamercommunity.ai.dto.AiPollResponse;
import com.gamercommunity.ai.entity.AiComment;
import com.gamercommunity.ai.repository.AiCommentRepository;
import com.gamercommunity.ai.service.AiCommentService;
import com.gamercommunity.ai.service.AiCommentWriter;
import com.gamercommunity.ai.usage.AiUsageService;
import com.gamercommunity.global.exception.custom.AiUsageLimitExceededException;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AiFacadeTest {

    @InjectMocks
    private AiFacade aiFacade;

    @Mock private UserRepository userRepository;
    @Mock private AiCommentRepository aiCommentRepository;
    @Mock private AiCommentService aiCommentService;
    @Mock private AiCommentWriter aiCommentWriter;
    @Mock private AiUsageService aiUsageService;

    @Test
    @DisplayName("AI 요청 - 존재하지 않는 사용자면 EntityNotFoundException")
    void ask_userNotFound() {
        // given
        given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> aiFacade.ask(1L, "unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("AI 요청 - 쿼터 초과 시 AiUsageLimitExceededException")
    void ask_quotaExceeded() {
        // given
        User user = User.builder().loginId("testUser").nickname("테스트").build();
        setId(user, 1L);
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(user));
        willThrow(new AiUsageLimitExceededException("횟수 초과"))
                .given(aiUsageService).checkQuota(1L);

        // when & then
        assertThatThrownBy(() -> aiFacade.ask(1L, "testUser"))
                .isInstanceOf(AiUsageLimitExceededException.class);
    }

    @Test
    @DisplayName("AI 요청 - 이미 진행 중이면 IllegalStateException")
    void ask_alreadyLocked() {
        // given
        User user = User.builder().loginId("testUser").nickname("테스트").build();
        setId(user, 1L);
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(user));
        willDoNothing().given(aiUsageService).checkQuota(1L);
        given(aiUsageService.tryLockUser(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> aiFacade.ask(1L, "testUser"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("진행 중");
    }

    @Test
    @DisplayName("AI 요청 - 정상 요청 시 taskId 반환")
    void ask_success() {
        // given
        User user = User.builder().loginId("testUser").nickname("테스트").build();
        setId(user, 1L);
        given(userRepository.findByLoginId("testUser")).willReturn(Optional.of(user));
        willDoNothing().given(aiUsageService).checkQuota(1L);
        given(aiUsageService.tryLockUser(1L)).willReturn(true);
        given(aiCommentWriter.createPendingRecord(1L, 1L)).willReturn(42L);

        // when
        AiAskResponse response = aiFacade.ask(1L, "testUser");

        // then
        assertThat(response.getTaskId()).isEqualTo(42L);
        assertThat(response.getMessage()).contains("생성 중");
        then(aiCommentService).should().generateAiCommentAsync(42L, 1L, 1L);
    }

    @Test
    @DisplayName("폴링 - PENDING 상태 조회")
    void poll_pending() {
        // given
        AiComment aiComment = AiComment.builder().postId(1L).userId(1L).build();
        setId(aiComment, 42L);
        given(aiCommentRepository.findById(42L)).willReturn(Optional.of(aiComment));

        // when
        AiPollResponse response = aiFacade.poll(42L);

        // then
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getContent()).isNull();
    }

    @Test
    @DisplayName("폴링 - COMPLETED 상태 조회")
    void poll_completed() {
        // given
        AiComment aiComment = AiComment.builder().postId(1L).userId(1L).build();
        setId(aiComment, 42L);
        aiComment.complete("AI 답변입니다.");
        given(aiCommentRepository.findById(42L)).willReturn(Optional.of(aiComment));

        // when
        AiPollResponse response = aiFacade.poll(42L);

        // then
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getContent()).isEqualTo("AI 답변입니다.");
    }

    @Test
    @DisplayName("폴링 - 존재하지 않는 taskId면 EntityNotFoundException")
    void poll_notFound() {
        // given
        given(aiCommentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> aiFacade.poll(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 헬퍼 ===

    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
