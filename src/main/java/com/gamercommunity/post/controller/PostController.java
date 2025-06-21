package com.gamercommunity.post.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;
import com.gamercommunity.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    // 카테고리별 게시글 목록
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostResponse>> getPostsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(name ="sort",defaultValue = "LATEST") PostSort postSort,
                                                                 @RequestParam(name ="tag", required = false) Tag tag) {
        return ResponseEntity.ok(postService.getPostsByCategoryAndSort(categoryId,postSort,tag));
    }
}
