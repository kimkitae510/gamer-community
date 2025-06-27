package com.gamercommunity.reviewLike.repository;

import com.gamercommunity.review.entity.Review;
import com.gamercommunity.reviewLike.entity.ReviewLike;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    // 존재 여부 확인 (toggleLike용)
    Optional<ReviewLike> findByUserAndReview(User user, Review review);


    // 리뷰 목록 좋아요 상태 확인
    @Query("""
        SELECT rl.review.id
        FROM ReviewLike rl
        WHERE rl.review.id IN :reviewIds AND rl.user.loginId = :loginId
    """)
    List<Long> findLikedReviewIds(@Param("reviewIds") List<Long> reviewIds, @Param("loginId") String loginId);

}
