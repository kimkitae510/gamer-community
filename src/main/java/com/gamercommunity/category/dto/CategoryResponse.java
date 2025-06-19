package com.gamercommunity.category.dto;


import com.gamercommunity.category.entity.Category;
import com.gamercommunity.genre.dto.GenreResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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



    public static CategoryResponse fromParent(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .writable(category.isWritable())
                .build();
    }


    public static CategoryResponse fromChild(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .writable(category.isWritable())
                .imageUrl(category.getImageUrl())
                .genres(category.getGenres().stream()
                        .map(GenreResponse::from)
                        .collect(Collectors.toList()))
                .children(null)
                .build();
    }



}
