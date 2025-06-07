package com.gamercommunity.user.controller;

import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.user.dto.JoinRequest;
import com.gamercommunity.user.dto.LoginRequest;
import com.gamercommunity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

private final UserService userService;

    // 로그인 ID 중복 체크
    @GetMapping("/check-id")
    public ResponseEntity<?> checkLoginId(@RequestParam String loginId) {
        boolean isDuplicate = userService.checkLoginIdDuplicate(loginId);
        return ResponseEntity.ok(isDuplicate);
    }


    // 닉네임 중복 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean isDuplicate = userService.checkNicknameDuplicate(username);
        return ResponseEntity.ok(isDuplicate);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("로그인 입력값이 유효하지 않습니다.");
        }

        try {
            TokenResponse token = userService.login(request);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid JoinRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("입력값이 유효하지 않습니다.");
        }

        if (userService.checkLoginIdDuplicate(request.getLoginId())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 로그인 아이디입니다.");
        }

        if (userService.checkNicknameDuplicate(request.getUsername())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 닉네임입니다.");
        }

        try {
            userService.join(request);
            return ResponseEntity.ok("회원가입 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
