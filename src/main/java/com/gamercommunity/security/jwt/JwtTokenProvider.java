package com.gamercommunity.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분


    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // Secret Key 길이 검증
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT Secret key must be at least 256 bits (32 bytes). Current length: " + keyBytes.length
            );
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT TokenProvider initialized with key length: {} bytes", keyBytes.length);
    }


    //Access Token 생성
    public String createToken(String loginId) {
        return createTokenInternal(loginId, ACCESS_TOKEN_EXPIRE_TIME);
    }


    //JWT 토큰 생성 (내부 메서드)
    private String createTokenInternal(String loginId, long expireTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
