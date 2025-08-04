package com.gamercommunity.post.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.post.dto.PostCreateRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.dto.PostUpdateRequest;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;
import com.gamercommunity.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Long> createPost(@RequestBody @Valid PostCreateRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        Long postId = postService.createPost(request, loginId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    // 게시글 단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(postService.updatePost(postId, request, loginId));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        postService.deletePost(postId, loginId);
        return ResponseEntity.noContent().build();
    }

    // 카테고리별 게시글 목록 (페이징)
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "sort", defaultValue = "LATEST") PostSort postSort,
            @RequestParam(name = "tag", required = false) Tag tag,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        return ResponseEntity.ok(postService.getPostsByCategoryWithPaging(categoryId, postSort, tag, pageable));
    }
}
