package com.gamercommunity.post.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;


    //게시글 작성
    @PostMapping
    public ResponseEntity<Long> createPost(@RequestBody PostRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));

        Long postId = postService.createPost(request, loginId);

        return ResponseEntity.ok(postId);
    }


    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        postService.deletePost(postId, loginId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId,
                                                   @RequestBody PostRequest postRequest) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));

        return ResponseEntity.ok(
                postService.updatePost(postId, postRequest, loginId)
        );
    }

    // 단일 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {

        return ResponseEntity.ok(postService.getPost(postId));
    }
}
