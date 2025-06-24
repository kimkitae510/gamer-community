package com.gamercommunity.review.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.DuplicateEntityException;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.review.repository.ReviewRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 리뷰 작성
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String loginId) {
        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Category game = categoryRepository.findById(request.getGameId())
                .orElseThrow(() -> new EntityNotFoundException("게임", request.getGameId()));

        boolean exists = reviewRepository.existsByAuthorAndGame(author, game);
        if (exists) {
            throw new DuplicateEntityException("이미 작성한 리뷰가 있습니다.");
        }

        Review review = Review.builder()
                .game(game)
                .author(author)
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        reviewRepository.save(review);

        categoryRepository.incrementReviewCount(request.getGameId());
        categoryRepository.recalculateRating(request.getGameId());

        return ReviewResponse.from(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰", reviewId));

        if (!review.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 리뷰만 삭제할 수 있습니다.");
        }
        Long gameId = review.getGame().getId();
        reviewRepository.delete(review);

        categoryRepository.decrementReviewCount(gameId);
        categoryRepository.recalculateRating(gameId);
    }

}
