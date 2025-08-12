package com.gamercommunity.auth.controller;

import com.gamercommunity.auth.dto.TokenRequest;
import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.auth.service.AuthService;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.security.jwt.JwtTokenProvider;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    //Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody TokenRequest tokenRequest) {
        log.info("토큰 재발급 요청");
        TokenResponse response = authService.reissue(tokenRequest);
        return ResponseEntity.ok(response);
    }



    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            throw new UnauthorizedException("잘못된 토큰 형식입니다.");
        }

        String accessToken = token.substring(7);

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        String loginId = jwtTokenProvider.getLoginIdFromToken(accessToken);
        authService.logout(loginId);

        return ResponseEntity.ok("로그아웃 완료");
    }

    // 인증정보
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMyInfo(@RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            throw new UnauthorizedException("잘못된 토큰 형식입니다.");
        }

        String jwt = token.substring(7);
        if (!jwtTokenProvider.validateToken(jwt)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        String loginId = jwtTokenProvider.getLoginIdFromToken(jwt);
        User user = userService.getUserByLoginId(loginId);

        return ResponseEntity.ok(Map.of(
                "loginId", user.getLoginId(),
                "username", user.getNickname(),
                "grade", user.getGrade() != null ? user.getGrade().name() : "LEVEL1"
        ));
    }
}
