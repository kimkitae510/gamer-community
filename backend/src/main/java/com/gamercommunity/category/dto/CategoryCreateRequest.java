package com.gamercommunity.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    @NotBlank(message = "카테고리 이름을 입력해주세요.")
    @Size(max = 100, message = "카테고리 이름은 100자 이내로 입력해주세요.")
    private String name;

    private Long parentId;

    private boolean writable;

    private List<Long> genreId;
}
