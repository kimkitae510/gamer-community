package com.gamercommunity.ai.dto;

import com.gamercommunity.ai.entity.AiComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 폴링 응답.
 * 프론트는 status가 COMPLETED가 될 때까지 주기적으로 조회한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class AiPollResponse {

    private Long taskId;
    private String status;   // PENDING, COMPLETED, FAILED
    private String content;  // COMPLETED일 때만 값이 있음

    public static AiPollResponse from(AiComment aiComment) {
        return AiPollResponse.builder()
                .taskId(aiComment.getId())
                .status(aiComment.getStatus().name())
                .content(aiComment.isCompleted() ? aiComment.getContent() : null)
                .build();
    }
}
