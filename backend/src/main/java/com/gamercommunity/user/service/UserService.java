package com.gamercommunity.user.service;

import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.auth.entity.RefreshToken;
import com.gamercommunity.auth.repository.RefreshTokenRepository;
import com.gamercommunity.global.exception.custom.DuplicateEntityException;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import com.gamercommunity.security.jwt.JwtTokenProvider;
import com.gamercommunity.user.dto.JoinRequest;
import com.gamercommunity.user.dto.LoginRequest;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void join(JoinRequest joinRequest) {
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            throw new InvalidRequestException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByLoginId(joinRequest.getLoginId())) {
            throw new DuplicateEntityException("아이디", joinRequest.getLoginId());
        }
        if (userRepository.existsByNickname(joinRequest.getNickname())) {
            throw new DuplicateEntityException("닉네임", joinRequest.getNickname());
        }

        String encodedPassword = passwordEncoder.encode(joinRequest.getPassword());
        User user = User.from(joinRequest, encodedPassword);
        userRepository.save(user);
    }

    // loginId로 사용자 조회
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginRequest.getLoginId()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());

        RefreshToken tokenEntity = new RefreshToken(user.getLoginId(), refreshToken);
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }
}
