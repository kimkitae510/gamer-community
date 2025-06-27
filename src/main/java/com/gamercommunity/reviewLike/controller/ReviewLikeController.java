package com.gamercommunity.reviewLike.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.reviewLike.dto.ReviewLikeResponse;
import com.gamercommunity.reviewLike.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // 리뷰 목록 좋아요 상태확인
    @GetMapping("/like-status/bulk")
    public ResponseEntity<Map<Long, Boolean>> getLikeStatusBulk(@RequestParam List<Long> reviewIds) {
        String loginId = SecurityUtil.getRequiredLoginId();
        Map<Long, Boolean> likeStatus = reviewLikeService.getLikeStatus(reviewIds, loginId);
        return ResponseEntity.ok(likeStatus);
    }
}
