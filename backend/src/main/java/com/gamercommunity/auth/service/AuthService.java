package com.gamercommunity.auth.service;

import com.gamercommunity.auth.dto.TokenRequest;
import com.gamercommunity.auth.dto.TokenResponse;
import com.gamercommunity.auth.entity.RefreshToken;
import com.gamercommunity.auth.repository.RefreshTokenRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import com.gamercommunity.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;



    //토큰 재발급
    public TokenResponse reissue(TokenRequest tokenRequest) {
        String accessToken = tokenRequest.getAccessToken();
        String refreshToken = tokenRequest.getRefreshToken();

        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRequestException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token DB 확인
        RefreshToken tokenInDb = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("DB에 Refresh Token이 없습니다: " + refreshToken));

        // Refresh Token에서 loginId 추출
        String loginId = jwtTokenProvider.getLoginIdFromToken(refreshToken);

        // Access Token이 아직 유효한 경우 그대로 반환
        if (jwtTokenProvider.validateToken(accessToken)) {
            log.debug("Access Token이 아직 유효합니다. loginId: {}", loginId);
            return TokenResponse.of(accessToken, refreshToken);
        }

        // Access Token이 만료된 경우 새로 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(loginId);
        log.info("Access Token 재발급 완료. loginId: {}", loginId);
        
        return TokenResponse.of(newAccessToken, refreshToken);
    }

    //로그아웃시 토큰삭제
    public void logout(String loginId) {
        Optional<RefreshToken> token = refreshTokenRepository.findByLoginId(loginId);
        token.ifPresent(refreshTokenRepository::delete);
    }
}
