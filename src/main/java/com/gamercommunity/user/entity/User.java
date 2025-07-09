package com.gamercommunity.user.entity;

import com.gamercommunity.user.dto.JoinRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(nullable = false)
    private Long id;

    @Column(unique = true)
    private String loginId;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    // 회원가입
    public static User from(JoinRequest request, String encodedPassword) {
        return User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .nickname(request.getUsername())
                .grade(Grade.LEVEL1)
                .build();
    }

}
