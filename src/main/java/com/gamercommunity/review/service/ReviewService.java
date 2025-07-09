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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


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

            // @멘션 추가
            content = "@" + target.getAuthor().getNickname() + " " + content;
            rating = null;  // 대댓글은 평점 없음
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

        // 원본 리뷰만 카운트/평점 업데이트
        if (parent == null) {
            categoryRepository.incrementReviewCount(request.getGameId());
            categoryRepository.recalculateRating(request.getGameId());
        }

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


        review.softDelete();

        // 원본 리뷰만 평점 재계산
        if (review.getParent() == null) {
            categoryRepository.recalculateRating(gameId);
        }
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰", reviewId));

        if (!review.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 리뷰만 수정할 수 있습니다.");
        }

        // 원본 리뷰 수정
        if (review.getParent() == null) {
            review.update(request.getContent(), request.getRating());
            categoryRepository.recalculateRating(review.getGame().getId());
        } else {
            // 대댓글 수정
            review.updateContent(request.getContent());
        }

        return ReviewResponse.from(review);
    }

    // 게임별 리뷰 트리 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByGame(Long gameId, String loginId) {
        Category game = categoryRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("게임", gameId));

        List<Review> allReviews = reviewRepository.findByGameWithAuthor(game);

        if (allReviews.isEmpty()) {
            return new ArrayList<>();
        }

        // Review -> ReviewResponse 변환
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
                    r.getStatus().getCode()
            );
            dtoMap.put(r.getId(), dto);
        }

        // 좋아요 정보 일괄 조회
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
                System.err.println("좋아요 정보 조회 실패: " + e.getMessage());
            }
        }

        // 리뷰 트리 2단계 구성
        List<ReviewResponse> result = new ArrayList<>();
        for (Review r : allReviews) {
            ReviewResponse dto = dtoMap.get(r.getId());
            if (dto == null) continue;

            if (r.getParent() == null) {
                // 원본 리뷰
                result.add(dto);
            } else {
                // 대댓글
                ReviewResponse parentDto = dtoMap.get(r.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return result;
    }

}
