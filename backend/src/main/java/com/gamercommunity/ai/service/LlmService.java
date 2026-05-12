package com.gamercommunity.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LlmService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    public String generateAnswer(String categoryName, String title, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userMessage = String.format(
                "%s 게임 관련 질문입니다. 친절하고 구체적으로 답변해주세요.\n\n제목: %s\n\n내용: %s",
                categoryName, title, content
        );

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "당신은 게임 커뮤니티의 AI 도우미입니다. 게이머들의 질문에 친절하고 정확하게 한국어로 답변해주세요."),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, entity, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (HttpClientErrorException e) {
            log.error("[LlmService] OpenAI API 호출 실패 - status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 답변 생성에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("[LlmService] OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 답변 생성에 실패했습니다.", e);
        }
    }
}
