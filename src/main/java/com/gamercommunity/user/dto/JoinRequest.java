package com.gamercommunity.user.dto;



import com.gamercommunity.user.entity.Grade;
import jakarta.validation.constraints.NotBlank;
import com.gamercommunity.user.entity.User;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank(message = "아이디가 비어있습니다.")
    private String loginId;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    @NotBlank(message = "비밀번호가 비어있습니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인이 비어있습니다.")
    private String passwordCheck;

    @NotBlank(message = "닉네임이 비어있습니다.")
    private String username;


    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(this.loginId)
                .password(encodedPassword)
                .nickname(this.username)
                .grade(Grade.LEVEL1)
                .build();
    }
}
