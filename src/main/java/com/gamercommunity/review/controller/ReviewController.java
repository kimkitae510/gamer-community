package com.gamercommunity.review.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
