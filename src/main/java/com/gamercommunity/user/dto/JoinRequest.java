package com.gamercommunity.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank(message = "아이디가 비어있습니다.")
    @Size(min = 8, max = 16, message = "아이디는 8자 이상 16자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = "아이디는 영어와 숫자만 가능합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호가 비어있습니다.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$", 
             message = "비밀번호는 영어, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인이 비어있습니다.")
    private String passwordCheck;

    @NotBlank(message = "닉네임이 비어있습니다.")
    private String nickname;

}
