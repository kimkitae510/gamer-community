package com.gamercommunity.review.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {


    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@RequestBody ReviewRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));

        ReviewResponse response = reviewService.createReview(request, loginId);
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));

        reviewService.deleteReview(reviewId, loginId);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId,
                                                       @RequestBody ReviewRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));

        ReviewResponse response = reviewService.updateReview(reviewId, request, loginId);
        return ResponseEntity.ok(response);
    }

    // 게임별 리뷰 목록 조회
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByGame(@PathVariable Long gameId) {
        String loginId = SecurityUtil.getCurrentLoginId().orElse(null);
        List<ReviewResponse> reviews = reviewService.getReviewsByGame(gameId, loginId);
        return ResponseEntity.ok(reviews);
    }
}
