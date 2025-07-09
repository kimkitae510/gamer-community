package com.gamercommunity.user.service;

import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.auth.entity.RefreshToken;
import com.gamercommunity.auth.repository.RefreshTokenRepository;
import com.gamercommunity.security.jwt.JwtTokenProvider;
import com.gamercommunity.user.dto.JoinRequest;
import com.gamercommunity.user.dto.LoginRequest;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    // 로그인 ID 중복 체크
    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    // 닉네임 중복 체크
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 회원가입
    public void join(JoinRequest joinRequest) {

        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByLoginId(joinRequest.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(joinRequest.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(joinRequest.getPassword());
        User user = User.from(joinRequest, encodedPassword);
        userRepository.save(user);
    }

    // loginId로 사용자 조회
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }


    // 로그인
    public TokenResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());


        RefreshToken tokenEntity = new RefreshToken(user.getLoginId(), refreshToken);
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(accessToken, refreshToken);

    }


}
