package com.gamercommunity.comment.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.comment.dto.CommentCreateRequest;
import com.gamercommunity.comment.dto.CommentResponse;
import com.gamercommunity.comment.dto.CommentUpdateRequest;
import com.gamercommunity.comment.service.CommentService;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 또는 대댓글 작성
    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody @Valid CommentCreateRequest dto) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        Long commentId = commentService.saveComment(dto, loginId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentId);
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest dto
    ) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        commentService.updateComment(commentId, dto.getContent(), loginId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        String loginId = SecurityUtil.getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
        commentService.deleteComment(commentId, loginId);
        return ResponseEntity.noContent().build();
    }

    // 게시글의 댓글 목록 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        String loginId = SecurityUtil.getCurrentLoginId().orElse(null);
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, loginId));
    }
}
