package com.gamercommunity.reviewLike.service;

import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.review.repository.ReviewRepository;
import com.gamercommunity.reviewLike.dto.ReviewLikeResponse;
import com.gamercommunity.reviewLike.entity.ReviewLike;
import com.gamercommunity.reviewLike.repository.ReviewLikeRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    //좋아요 토글(추가/취소)
    @Transactional
    public ReviewLikeResponse toggleLike(Long reviewId, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰", reviewId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserAndReview(user, review);

        boolean liked;
        if (existingLike.isPresent()) {
            reviewRepository.decrementLikeCount(reviewId);
            reviewLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            // 좋아요 추가
            try {
                reviewLikeRepository.save(ReviewLike.builder()
                        .review(review)
                        .user(user)
                        .build());
                reviewRepository.incrementLikeCount(reviewId);
                liked = true;
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
            }
        }

        // 엔티티 값 직접 계산 (추가 SELECT 불필요)
        int updatedLikeCount = review.getLikeCount() + (liked ? 1 : -1);

        return ReviewLikeResponse.builder()
                .id(reviewId)
                .likeCount(updatedLikeCount)
                .liked(liked)
                .build();
    }

    // 리뷰 목록 좋아요 상태 조회 — 단순 SELECT, 트랜잭션 불필요
    public Map<Long, Boolean> getLikeStatus(List<Long> reviewIds, String loginId) {
        List<Long> likedReviewIdList = reviewLikeRepository.findLikedReviewIds(reviewIds, loginId);
        Set<Long> likedReviewIds = new HashSet<>(likedReviewIdList); // List → Set 변환

        Map<Long, Boolean> result = new HashMap<>();
        for (Long reviewId : reviewIds) {
            result.put(reviewId, likedReviewIds.contains(reviewId)); // O(1) 조회
        }

        return result;
    }
}
