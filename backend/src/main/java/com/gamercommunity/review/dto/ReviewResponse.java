package com.gamercommunity.review.dto;

import com.gamercommunity.review.entity.Review;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private String content;
    private String authorName;
    private String authorId;
    private Integer rating;  // 원본 리뷰만, 대댓글은 null
    private String createdAt;
    private String updatedAt;
    private List<ReviewResponse> children;
    private int likeCount;
    
    @Setter
    private Boolean isLiked;
    
    private String status;

    public static ReviewResponse of(
            Long id,
            String content,
            String authorName,
            String authorId,
            Integer rating,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int likeCount,
            String status
    ) {
        return ReviewResponse.builder()
                .id(id)
                .content(content)
                .authorName(authorName)
                .authorId(authorId)
                .rating(rating)  // null일 수 있음 (대댓글)
                .createdAt(createdAt.toString())
                .updatedAt(updatedAt.toString())
                .children(new ArrayList<>())
                .likeCount(likeCount)
                .isLiked(false)
                .status(status)
                .build();
    }

    // 기존 호환성 유지
    public static ReviewResponse from(Review review) {
        return of(
                review.getId(),
                review.getContent(),
                review.getAuthor().getNickname(),
                review.getAuthor().getLoginId(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getLikeCount(),
                review.getStatus().name()
        );
    }
}
