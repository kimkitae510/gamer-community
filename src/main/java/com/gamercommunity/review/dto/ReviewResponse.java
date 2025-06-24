package com.gamercommunity.review.dto;

import com.gamercommunity.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ReviewResponse {

    private Long id;
    private String author;
    private String authorId;
    private int rating;
    private String content;
    private String createdAt;
    private String updatedAt;
    private long gameId;


    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .author(review.getAuthor().getNickname())
                .authorId(review.getAuthor().getLoginId())
                .rating(review.getRating())
                .content(review.getContent())
                .gameId(review.getGame().getId())
                .createdAt(review.getCreatedAt().toString())
                .updatedAt(review.getUpdatedAt().toString())
                .build();
    }
}
