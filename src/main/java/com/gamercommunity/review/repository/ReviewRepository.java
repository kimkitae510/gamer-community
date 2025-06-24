package com.gamercommunity.review.repository;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.review.entity.Review;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.game = :game")
    List<Review> findByGameWithAuthor(@Param("game") Category game);

    boolean existsByAuthorAndGame(User author, Category game);

}
