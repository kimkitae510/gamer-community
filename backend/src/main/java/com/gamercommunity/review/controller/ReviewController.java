package com.gamercommunity.review.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ReviewResponse> createReview(@RequestBody @Valid ReviewRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(request, loginId));
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequest request) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, loginId));
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        reviewService.deleteReview(reviewId, loginId);
        return ResponseEntity.noContent().build();
    }

    // 게임별 리뷰 목록 조회
    @GetMapping("/games/{gameId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByGame(@PathVariable Long gameId) {
        String loginId = SecurityUtil.getCurrentLoginId().orElse(null);
        return ResponseEntity.ok(reviewService.getReviewsByGame(gameId, loginId));
    }
}
