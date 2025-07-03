package com.gamercommunity.commentLike.repository;

import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.commentLike.entity.CommentLike;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 존재 여부 확인
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    // 댓글 목록 좋아요 여부 확인
    @Query("""
        SELECT cl.comment.id
        FROM CommentLike cl
        WHERE cl.comment.id IN :commentIds AND cl.user.loginId = :loginId
    """)
    List<Long> findLikedCommentIds(@Param("commentIds") List<Long> commentIds, @Param("loginId") String loginId);

}
