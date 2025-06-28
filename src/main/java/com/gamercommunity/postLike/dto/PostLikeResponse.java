package com.gamercommunity.postLike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostLikeResponse {

    private Long id;
    private boolean liked;
    private long likeCount;

}
