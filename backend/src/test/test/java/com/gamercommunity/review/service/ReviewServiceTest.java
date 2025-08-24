package com.gamercommunity.review.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.review.repository.ReviewRepository;
import com.gamercommunity.reviewLike.repository.ReviewLikeRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewLikeRepository reviewLikeRepository;

    @Test
    @DisplayName("리뷰 삭제 - 존재하지 않는 리뷰면 EntityNotFoundException")
    void deleteReview_notFound() {
        // given
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(999L, "testUser"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 - 본인이 아닌 사용자가 삭제 시도하면 AccessDeniedException")
    void deleteReview_notAuthor() {
        // given
        Review review = createReview(1L, "author1");
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(1L, "otherUser"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 - 이미 삭제된 리뷰면 EntityNotFoundException")
    void deleteReview_alreadyDeleted() {
        // given
        Review review = createReview(1L, "testUser");
        review.softDelete();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(1L, "testUser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("삭제된");
    }

    @Test
    @DisplayName("리뷰 수정 - 이미 삭제된 리뷰면 EntityNotFoundException")
    void updateReview_alreadyDeleted() {
        // given
        Review review = createReview(1L, "testUser");
        review.softDelete();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(1L, null, "testUser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("삭제된");
    }

    // === 헬퍼 ===

    private Review createReview(Long id, String loginId) {
        User author = User.builder().loginId(loginId).nickname("닉네임").build();
        Category game = Category.builder().name("테스트게임").writable(true).build();
        setId(game, 100L);

        Review review = Review.builder()
                .game(game)
                .author(author)
                .content("리뷰 내용")
                .rating(5)
                .parent(null)
                .build();

        setId(review, id);
        return review;
    }

    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
