package com.gamercommunity.reviewLike.repository;

import com.gamercommunity.review.entity.Review;
import com.gamercommunity.reviewLike.entity.ReviewLike;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    // 존재 여부 확인 (toggleLike용)
    Optional<ReviewLike> findByUserAndReview(User user, Review review);


}
