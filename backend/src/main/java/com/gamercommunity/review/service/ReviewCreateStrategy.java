package com.gamercommunity.review.service;

import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;

// 리뷰 생성 전략 인터페이스
// 낙관적 락 / 비관적 락 / 기본 방식 등 다양한 동시성 전략을 통합
public interface ReviewCreateStrategy {

    ReviewResponse createReview(ReviewRequest request, String loginId);
}
