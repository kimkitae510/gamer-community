package com.gamercommunity.post.repository;

import com.gamercommunity.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

}
