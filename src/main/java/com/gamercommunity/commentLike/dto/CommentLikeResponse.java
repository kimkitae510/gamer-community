package com.gamercommunity.commentLike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class CommentLikeResponse {

    private Long id;
    private int likeCount;
    private boolean isLiked;

}
