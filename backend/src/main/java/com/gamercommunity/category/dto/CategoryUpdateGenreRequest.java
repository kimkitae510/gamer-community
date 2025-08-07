package com.gamercommunity.category.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateGenreRequest {

    @NotEmpty(message = "장르를 최소 하나 이상 선택해주세요.")
    private List<Long> genreId;
}
