package com.gamercommunity.auth.controller;

import com.gamercommunity.auth.dto.TokenRequest;
import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody TokenRequest tokenRequest) {
        log.info("토큰 재발급 요청");
        TokenResponse response = authService.reissue(tokenRequest);
        return ResponseEntity.ok(response);
    }
}
