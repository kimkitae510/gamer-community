package com.gamercommunity.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * AI 질문 요청의 응답.
 * 프론트에서 이 taskId로 폴링한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class AiAskResponse {
    private Long taskId;
    private String message;
}
