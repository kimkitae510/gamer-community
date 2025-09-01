package com.gamercommunity.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {

    @NotNull(message = "게시글 ID가 필요합니다.")
    private Long postId;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 2000, message = "댓글은 2000자 이내로 입력해주세요.")
    private String content;

    private Long parentId;
}
