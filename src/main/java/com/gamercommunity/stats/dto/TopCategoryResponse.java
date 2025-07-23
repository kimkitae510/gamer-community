package com.gamercommunity.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TopCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Long postCount;
    private Integer rank;
    private Double rating;
    private String imageUrl;
    private List<String> genres;
}
