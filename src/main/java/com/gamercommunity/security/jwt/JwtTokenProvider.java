package com.gamercommunity.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    //Secret Key 초기화 및 검증
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);


        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT Secret key must be at least 256 bits (32 bytes). Current length: " + keyBytes.length
            );
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT TokenProvider initialized with key length: {} bytes", keyBytes.length);
    }


    //Access Token 생성
    public String createAccessToken(String loginId) {
        return createTokenInternal(loginId, ACCESS_TOKEN_EXPIRE_TIME);
    }

    //Refresh Token 생성
    public String createRefreshToken(String loginId) {
        return createTokenInternal(loginId, REFRESH_TOKEN_EXPIRE_TIME);
    }


    //JWT 토큰 생성 (내부 메서드)
    private String createTokenInternal(String loginId, long expireTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .subject(loginId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    //JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다.");
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    //JWT에서 사용자 정보 추출
    public String getLoginIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //HTTP 요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
