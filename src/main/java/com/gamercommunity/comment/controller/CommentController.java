package com.gamercommunity.comment.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.comment.dto.CommentRequest;
import com.gamercommunity.comment.service.CommentService;
import com.gamercommunity.global.exception.custom.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}