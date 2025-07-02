package com.gamercommunity.commentLike.repository;

import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.commentLike.entity.CommentLike;
import com.gamercommunity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 존재 여부 확인
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

}
