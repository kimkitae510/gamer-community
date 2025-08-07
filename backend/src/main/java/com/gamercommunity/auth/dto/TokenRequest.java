package com.gamercommunity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(message = "Access Token이 필요합니다.")
    private String accessToken;

    @NotBlank(message = "Refresh Token이 필요합니다.")
    private String refreshToken;
}
