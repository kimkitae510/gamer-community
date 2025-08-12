package com.gamercommunity.popular.dto;

import com.gamercommunity.popular.entity.PopularScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TrendingPostResponse {
    private Long postId;
    private String title;
    private String author;
    private String authorId;
    private Long categoryId;
    private String categoryName;
    private int views;
    private int likeCount;
    private int commentCount;
    private String createdAt;
    private String tag;
    
    // 인기점수 정보
    private Integer popularScore;
    private Integer commentScore;
    private Integer likeScore;
    private Integer viewScore;

    public static TrendingPostResponse from(PopularScore popularScore) {
        return TrendingPostResponse.builder()
                .postId(popularScore.getPost().getId())
                .title(popularScore.getPost().getTitle())
                .author(popularScore.getPost().getAuthor().getNickname())
                .authorId(popularScore.getPost().getAuthor().getLoginId())
                .categoryId(popularScore.getPost().getCategory().getId())
                .categoryName(popularScore.getPost().getCategory().getName())
                .views(popularScore.getPost().getViews())
                .likeCount(popularScore.getPost().getLikeCount())
                .commentCount(popularScore.getPost().getCommentCount())
                .createdAt(popularScore.getPost().getCreatedAt().toString())
                .tag(popularScore.getPost().getTag() != null ? popularScore.getPost().getTag().toString() : "일반")
                .popularScore(popularScore.getScore())
                .commentScore(popularScore.getCommentScore())
                .likeScore(popularScore.getLikeScore())
                .viewScore(popularScore.getViewScore())
                .build();
    }
}
