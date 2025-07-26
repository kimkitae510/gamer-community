package com.gamercommunity.commentLike.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.commentLike.dto.CommentLikeResponse;
import com.gamercommunity.commentLike.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    //댓글 좋아요 토글(좋아요/취소)
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> toggleLike(@PathVariable Long commentId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        CommentLikeResponse commentLikeResponse = commentLikeService.toggleLike(commentId, loginId);
        return ResponseEntity.ok(commentLikeResponse);
    }

    // 개별 댓글 좋아요 상태 조회
    @GetMapping("/{commentId}/like-status")
    public ResponseEntity<CommentLikeResponse> getLikeStatus(@PathVariable Long commentId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        CommentLikeResponse likeStatus = commentLikeService.getLikeStatusSingle(commentId, loginId);
        return ResponseEntity.ok(likeStatus);
    }

    // 댓글목록 좋아요 상태조회
    @GetMapping("/like-status")
    public ResponseEntity<Map<Long, Boolean>> getLikeStatusBulk(@RequestParam List<Long> commentIds) {
        String loginId = SecurityUtil.getRequiredLoginId();
        Map<Long, Boolean> likeStatus = commentLikeService.getLikeStatus(commentIds, loginId);
        return ResponseEntity.ok(likeStatus);
    }

}
