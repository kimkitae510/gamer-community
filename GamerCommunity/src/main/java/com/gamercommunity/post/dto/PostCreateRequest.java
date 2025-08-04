package com.gamercommunity.post.dto;

import com.gamercommunity.post.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 50000, message = "내용은 50000자 이내로 입력해주세요.")
    private String content;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;

    @NotNull(message = "태그를 선택해주세요.")
    private Tag tag;
}
