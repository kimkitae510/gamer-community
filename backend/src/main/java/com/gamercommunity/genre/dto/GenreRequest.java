package com.gamercommunity.genre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreRequest(
        @NotBlank(message = "장르 이름은 필수입니다.")
        @Size(max = 50, message = "장르 이름은 50자를 초과할 수 없습니다.")
        String name
) {
}
