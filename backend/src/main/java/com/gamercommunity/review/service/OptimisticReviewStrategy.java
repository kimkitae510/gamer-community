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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

// 낙관적 락 방식 리뷰 생성 전략
// JPA @Version 기반 충돌 감지 + 지수 백오프 재시도
@Slf4j
@Service("optimisticReviewStrategy")
@RequiredArgsConstructor
public class OptimisticReviewStrategy implements ReviewCreateStrategy {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 30;

    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse createReview(ReviewRequest request, String loginId) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                ReviewResponse response = doCreateReview(request, loginId);
                if (attempt > 0) {
                    log.info("[낙관적 락 재시도 성공] attempt={}, gameId={}", attempt, request.getGameId());
                }
                return response;
            } catch (Exception e) {
                if (!isRetryable(e)) {
                    throw e;
                }

                log.warn("[낙관적 락 충돌] attempt={}/{}, gameId={}", attempt + 1, MAX_RETRIES, request.getGameId());

                if (attempt == MAX_RETRIES) {
                    throw new RuntimeException("낙관적 락 재시도 횟수 초과", e);
                }

                try {
                    long maxDelay = BASE_DELAY_MS * (1L << attempt);
                    Thread.sleep(ThreadLocalRandom.current().nextLong(0, maxDelay + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                }
            }
        }
        throw new RuntimeException("unreachable");
    }

    @Transactional
    protected ReviewResponse doCreateReview(ReviewRequest request, String loginId) {
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

        // 낙관적 락: AVG 계산 → 엔티티 필드 변경 → flush 시 @Version 체크
        if (parent == null) {
            Double avgRating = categoryRepository.calculateAvgRating(request.getGameId());
            game.updateRatingForTest(avgRating);
        }

        return ReviewResponse.from(review);
    }

    private boolean isRetryable(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String name = cause.getClass().getName().toLowerCase();
            String msg = cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
            if (name.contains("optimistic") || name.contains("stale")
                    || msg.contains("row was updated or deleted")
                    || msg.contains("deadlock")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
