package com.gamercommunity.postLike.repository;

import com.gamercommunity.post.entity.Post;
import com.gamercommunity.postLike.entity.PostLike;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);


    //해당 게시글의 모든 좋아요 삭제
    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
