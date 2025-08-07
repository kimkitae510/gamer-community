package com.gamercommunity.review.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.DuplicateEntityException;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.review.dto.ReviewRequest;
import com.gamercommunity.review.dto.ReviewResponse;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.review.repository.ReviewRepository;
import com.gamercommunity.reviewLike.repository.ReviewLikeRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    // 리뷰 작성
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

            if (target.getParent() == null) {
                parent = target;
            } else {
                parent = target.getParent();
            }

            content = "@" + target.getAuthor().getNickname() + " " + content;
            rating = null;
        } else {
            boolean exists = reviewRepository.existsByAuthorAndGameAndParentIsNull(author, game);
            if (exists) {
                throw new DuplicateEntityException("이미 작성한 리뷰가 있습니다.");
            }

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
            categoryRepository.addRating(request.getGameId(), rating);
        }

        return ReviewResponse.from(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰", reviewId));

        if (review.getStatus().isDeleted()) {
            throw new EntityNotFoundException("이미 삭제된 리뷰입니다. id=" + reviewId);
        }

        if (!review.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 리뷰만 삭제할 수 있습니다.");
        }

        Long gameId = review.getGame().getId();
        review.softDelete();

        if (review.getParent() == null) {
            categoryRepository.subtractRating(gameId, review.getRating());
        }
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰", reviewId));

        if (review.getStatus().isDeleted()) {
            throw new EntityNotFoundException("이미 삭제된 리뷰입니다. id=" + reviewId);
        }

        if (!review.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 리뷰만 수정할 수 있습니다.");
        }

        if (review.getParent() == null) {
            int oldRating = review.getRating();
            review.update(request.getContent(), request.getRating());
            categoryRepository.updateRatingDiff(review.getGame().getId(), oldRating, review.getRating());
        } else {
            review.updateContent(request.getContent());
        }

        return ReviewResponse.from(review);
    }

    // 게임별 리뷰 트리 조회 — JOIN FETCH 한 방 + 단순 SELECT, 트랜잭션 불필요
    public List<ReviewResponse> getReviewsByGame(Long gameId, String loginId) {
        Category game = categoryRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("게임", gameId));

        List<Review> allReviews = reviewRepository.findByGameWithAuthor(game);

        if (allReviews.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, ReviewResponse> dtoMap = new HashMap<>();
        for (Review r : allReviews) {
            ReviewResponse dto = ReviewResponse.of(
                    r.getId(),
                    r.getContent(),
                    r.getAuthor().getNickname(),
                    r.getAuthor().getLoginId(),
                    r.getRating(),
                    r.getCreatedAt(),
                    r.getUpdatedAt(),
                    r.getLikeCount(),
                    r.getStatus().name()
            );
            dtoMap.put(r.getId(), dto);
        }

        if (loginId != null && !loginId.isBlank()) {
            try {
                List<Long> reviewIds = allReviews.stream()
                        .map(Review::getId)
                        .collect(Collectors.toList());

                List<Long> likedReviewIdList = reviewLikeRepository.findLikedReviewIds(reviewIds, loginId);
                Set<Long> likedReviewIds = new HashSet<>(likedReviewIdList);

                dtoMap.values().forEach(dto ->
                        dto.setIsLiked(likedReviewIds.contains(dto.getId()))
                );
            } catch (Exception e) {
                log.warn("좋아요 정보 조회 실패: {}", e.getMessage());
            }
        }

        List<ReviewResponse> result = new ArrayList<>();
        for (Review r : allReviews) {
            ReviewResponse dto = dtoMap.get(r.getId());
            if (dto == null) continue;

            if (r.getParent() == null) {
                result.add(dto);
            } else {
                ReviewResponse parentDto = dtoMap.get(r.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return result;
    }

}
