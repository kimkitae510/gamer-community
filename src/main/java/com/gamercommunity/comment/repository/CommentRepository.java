package com.gamercommunity.comment.repository;

import com.gamercommunity.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    // 좋아요 수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    // 좋아요 수 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END WHERE c.id = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);

    // likeCount 조회
    @Query("SELECT c.likeCount FROM Comment c WHERE c.id = :commentId")
    Integer getLikeCount(@Param("commentId") Long commentId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);
}

