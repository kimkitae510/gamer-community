package com.gamercommunity.commentLike.service;

import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.dto.CommentLikeResponse;
import com.gamercommunity.commentLike.entity.CommentLike;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;


    //댓글 좋아요 토글(좋아요/취소)
    @Transactional
    public CommentLikeResponse toggleLike(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));


        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        boolean liked;
        if (existingLike.isPresent()) {
            // 좋아요 취소
            commentRepository.decrementLikeCount(commentId);
            commentLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            // 좋아요 추가
            try {
                commentLikeRepository.save(CommentLike.builder()
                        .comment(comment)
                        .user(user)
                        .build());
                commentRepository.incrementLikeCount(commentId);
                liked = true;
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
            }
        }

        Integer likeCount = commentRepository.getLikeCount(commentId);

        return CommentLikeResponse.builder()
                .id(commentId)
                .likeCount(likeCount != null ? likeCount : 0)
                .liked(liked)
                .build();
    }
}

