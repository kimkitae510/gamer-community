package com.gamercommunity.review.repository;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.game = :game")
    List<Review> findByGameWithAuthor(@Param("game") Category game);

    // 원본 리뷰만 확인 (평점이 있는 리뷰, 삭제되지 않은 것만)
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.author = :author AND r.game = :game AND r.parent IS NULL AND r.status = 'ACTIVE'")
    boolean existsByAuthorAndGameAndParentIsNull(@Param("author") User author, @Param("game") Category game);

    // 좋아요 수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    //좋아요 수 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = CASE WHEN r.likeCount > 0 THEN r.likeCount - 1 ELSE 0 END WHERE r.id = :reviewId")
    void decrementLikeCount(@Param("reviewId") Long reviewId);

    // likeCount만 조회
    @Query("SELECT r.likeCount FROM Review r WHERE r.id = :reviewId")
    Integer getLikeCount(@Param("reviewId") Long reviewId);

}
