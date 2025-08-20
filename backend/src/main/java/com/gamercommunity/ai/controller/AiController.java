package com.gamercommunity.ai.controller;

import com.gamercommunity.ai.dto.AiAskResponse;
import com.gamercommunity.ai.dto.AiPollResponse;
import com.gamercommunity.ai.facade.AiFacade;
import com.gamercommunity.ai.usage.AiUsageService;
import com.gamercommunity.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiFacade aiFacade;
    private final AiUsageService aiUsageService;

    // AI 질문 요청 (비동기) — taskId를 반환, 프론트는 이 ID로 폴링
    @PostMapping("/ask/{postId}")
    public ResponseEntity<AiAskResponse> askAi(@PathVariable Long postId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(aiFacade.ask(postId, loginId));
    }

    // 폴링: AI 답변 생성 진행 상태 조회
    // 프론트에서 1~2초 간격으로 호출
    // PENDING → 생성 중 / COMPLETED → content에 답변 포함 / FAILED → 실패
    @GetMapping("/poll/{taskId}")
    public ResponseEntity<AiPollResponse> pollResult(@PathVariable Long taskId) {
        return ResponseEntity.ok(aiFacade.poll(taskId));
    }

    // 남은 AI 사용 횟수 조회
    @GetMapping("/usage/remaining")
    public ResponseEntity<Integer> getRemainingUsage() {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(aiUsageService.getRemainingCount(loginId));
    }
}
