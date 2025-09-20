package com.gamercommunity.reviewLike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class ReviewLikeResponse {

    private Long id;
    private boolean liked;
    private int likeCount;


}
