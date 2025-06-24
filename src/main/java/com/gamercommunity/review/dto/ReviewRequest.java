package com.gamercommunity.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequest {

    private Long gameId;
    private int rating;
    private String content;

}
