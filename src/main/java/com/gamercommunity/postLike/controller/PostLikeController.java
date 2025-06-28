package com.gamercommunity.postLike.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.postLike.dto.PostLikeResponse;
import com.gamercommunity.postLike.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;


    // 좋아요 토글 (추가/취소)
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> toggleLike(@PathVariable Long postId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        PostLikeResponse postLikeResponse = postLikeService.toggleLike(postId, loginId);
        return ResponseEntity.ok(postLikeResponse);
    }
}
