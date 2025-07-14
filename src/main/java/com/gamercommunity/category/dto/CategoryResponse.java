package com.gamercommunity.category.dto;


import com.gamercommunity.category.entity.Category;
import com.gamercommunity.genre.dto.GenreResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private boolean writable;
    private List<CategoryResponse> children;
    private Double rating;
    private LocalDateTime createdAt;
    private List<GenreResponse> genres;
    private String imageUrl;
    private Long postCount;
    private Long ratingCount;



    public static CategoryResponse fromParent(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .writable(category.isWritable())
                .build();
    }


    public static CategoryResponse fromChild(Category category, List<GenreResponse> genres) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .writable(category.isWritable())
                .imageUrl(category.getImageUrl())
                .genres(genres)
                .rating(category.getRating())
                .postCount(category.getPostCount())
                .ratingCount(category.getReviewCount())
                .children(null)
                .build();
    }



}
