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

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 댓글 좋아요 토글 — save/delete + @Modifying 여러 개 → 트랜잭션 필요
    @Transactional
    public CommentLikeResponse toggleLike(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        boolean liked;
        if (existingLike.isPresent()) {
            commentRepository.decrementLikeCount(commentId);
            commentLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
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

        // 추가 SELECT 없이 메모리에서 계산
        return CommentLikeResponse.builder()
                .id(commentId)
                .likeCount(comment.getLikeCount() + (liked ? 1 : -1))
                .isLiked(liked)
                .build();
    }

    // 개별 댓글 좋아요 상태 조회 — 단순 SELECT, 트랜잭션 불필요
    public CommentLikeResponse getLikeStatusSingle(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        boolean isLiked = commentLikeRepository.findByUserAndComment(user, comment).isPresent();

        return CommentLikeResponse.builder()
                .id(commentId)
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .build();
    }

    // 댓글 목록 좋아요 상태 조회 — 단순 SELECT, 트랜잭션 불필요
    public Map<Long, Boolean> getLikeStatus(List<Long> commentIds, String loginId) {
        Set<Long> likedIds = new HashSet<>(commentLikeRepository.findLikedCommentIds(commentIds, loginId));

        Map<Long, Boolean> result = new HashMap<>();
        for (Long commentId : commentIds) {
            result.put(commentId, likedIds.contains(commentId));
        }
        return result;
    }
}
