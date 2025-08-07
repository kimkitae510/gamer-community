package com.gamercommunity.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "게임 ID가 필요합니다.")
    private Long gameId;

    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(max = 5000, message = "리뷰는 5000자 이내로 입력해주세요.")
    private String content;

    private Long parentId;
}
