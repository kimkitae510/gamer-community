package com.gamercommunity.reviewLike.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.reviewLike.dto.ReviewLikeResponse;
import com.gamercommunity.reviewLike.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewLikeController {


    private final ReviewLikeService reviewLikeService;


    // 좋아요 토글 (추가/취소)
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeResponse> toggleLike(@PathVariable Long reviewId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        ReviewLikeResponse reviewLikeResponse = reviewLikeService.toggleLike(reviewId, loginId);
        return ResponseEntity.ok(reviewLikeResponse);
    }
}
