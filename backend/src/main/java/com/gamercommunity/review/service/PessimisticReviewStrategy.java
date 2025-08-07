package com.gamercommunity.review.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.review.repository.ReviewRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 비관적 락 방식 리뷰 생성 전략
// Category row에 X-lock을 걸어 동시성 충돌을 방지
@Service("pessimisticReviewStrategy")
@RequiredArgsConstructor
public class PessimisticReviewStrategy implements ReviewCreateStrategy {

    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String loginId) {
        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Category game = categoryRepository.findById(request.getGameId())
                .orElseThrow(() -> new EntityNotFoundException("게임", request.getGameId()));

        Review parent = null;
        String content = request.getContent();
        Integer rating = request.getRating();

        if (request.getParentId() != null) {
            Review target = reviewRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 리뷰", request.getParentId()));
            parent = (target.getParent() == null) ? target : target.getParent();
            content = "@" + target.getAuthor().getNickname() + " " + content;
            rating = null;
        } else {
            if (rating == null) {
                throw new IllegalArgumentException("원본 리뷰는 평점이 필수입니다.");
            }
        }

        Review review = Review.builder()
                .game(game)
                .author(author)
                .content(content)
                .rating(rating)
                .parent(parent)
                .build();

        if (parent != null) {
            parent.addChild(review);
        }

        reviewRepository.save(review);

        if (parent == null) {
            // 비관적 락: game row X-lock → AVG 계산 → 반영
            categoryRepository.findByIdForUpdate(request.getGameId());
            Double avgRating = categoryRepository.calculateAvgRating(request.getGameId());
            categoryRepository.updateRating(request.getGameId(), avgRating);
            categoryRepository.incrementReviewCount(request.getGameId());
        }

        return ReviewResponse.from(review);
    }
}
