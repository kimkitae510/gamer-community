package com.gamercommunity.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequest {

    private Long gameId;
    private Integer rating;  // 원본 리뷰만, 대댓글은 null
    private String content;
    private Long parentId;   // 대댓글용 (null이면 원본 리뷰)

}
