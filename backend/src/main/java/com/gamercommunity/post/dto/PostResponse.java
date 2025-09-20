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
                .tag(post.getTag() != null ? post.getTag().toString() : "일반")
                .build();
    }

    // 메모리 조회수 버전 - DB 저장값 + 미flush 메모리값을 합산하여 반환
    public static PostResponse from(Post post, long realTimeViews) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())
                .authorId(post.getAuthor().getLoginId())
                .categoryId(post.getCategory().getId())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .views((int) realTimeViews)
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .tag(post.getTag() != null ? post.getTag().toString() : "일반")
                .build();
    }
}
