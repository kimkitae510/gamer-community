package com.gamercommunity.post.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;


    //게시글 작성
    @PostMapping
    public ResponseEntity<Long> createPost(@RequestBody PostRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new RuntimeException("로그인 사용자 아님"));

        Long postId = postService.createPost(request, loginId);

        return ResponseEntity.ok(postId);
    }

}
