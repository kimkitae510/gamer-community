package com.gamercommunity.commentLike.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.commentLike.dto.CommentLikeResponse;
import com.gamercommunity.commentLike.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
