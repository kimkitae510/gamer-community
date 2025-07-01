package com.gamercommunity.comment.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.comment.dto.CommentRequest;
import com.gamercommunity.comment.service.CommentService;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //댓글 또는 대댓글 작성
    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody CommentRequest dto) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));
        Long commentId = commentService.saveComment(dto, loginId);
        return ResponseEntity.ok(commentId);
    }

    //댓글삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));
        commentService.deleteComment(commentId, loginId);
        return ResponseEntity.noContent().build();
    }

}