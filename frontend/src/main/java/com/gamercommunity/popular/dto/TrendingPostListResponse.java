package com.gamercommunity.popular.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TrendingPostListResponse {
    private List<TrendingPostResponse> posts;
    private String description;
    private int totalCount;

    public static TrendingPostListResponse of(List<TrendingPostResponse> posts, String description) {
        return TrendingPostListResponse.builder()
                .posts(posts)
                .description(description)
                .totalCount(posts.size())
                .build();
    }
}
