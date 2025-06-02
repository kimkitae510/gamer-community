package com.gamercommunity.user.controller;

import com.gamercommunity.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

private UserService userService;

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

}
