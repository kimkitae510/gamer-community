package com.gamercommunity.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String author;
    private String authorId;
    private String createdAt;
    private String updatedAt;
    private List<CommentResponse> replies;
    private String postAuthorName;
    private int likeCount;

    @Setter
    private Boolean isLiked;

    public static CommentResponse of(
            Long id,
            String content,
            String authorName,
            String authorId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int likeCount
    ) {
        return CommentResponse.builder()
                .id(id)
                .content(content)
                .author(authorName)
                .authorId(authorId)
                .createdAt(createdAt.toString())
                .updatedAt(updatedAt.toString())
                .replies(new ArrayList<>())
                .likeCount(likeCount)
                .isLiked(false)
                .build();
    }
}
