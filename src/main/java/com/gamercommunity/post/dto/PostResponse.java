package com.gamercommunity.post.dto;

import com.gamercommunity.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String authorId;
    private Long categoryId;
    private int views;
    private String createdAt;
    private String updatedAt;
    private int likeCount;
    private int commentCount;
    private String tag;


    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())
                .authorId(post.getAuthor().getLoginId())
                .categoryId(post.getCategory().getId())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .views(post.getViews())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .tag(post.getTag().toString())
                .build();
    }
}
