package com.gamercommunity.user.controller;

import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.user.dto.JoinRequest;
import com.gamercommunity.user.dto.LoginRequest;
import com.gamercommunity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 로그인 ID 중복 체크
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkLoginId(@RequestParam String loginId) {
        boolean isDuplicate = userService.checkLoginIdDuplicate(loginId);
        return ResponseEntity.ok(isDuplicate);
    }


    // 닉네임 중복 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean isDuplicate = userService.checkNicknameDuplicate(username);
        return ResponseEntity.ok(isDuplicate);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody @Valid JoinRequest request) {
        userService.join(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

}
