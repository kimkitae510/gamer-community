package com.gamercommunity.comment.dto;

import lombok.Getter;

@Getter
public class CommentRequest {
    private Long postId;
    private String content;
    private Long parentId;
}
