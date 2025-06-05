package com.gamercommunity.auth.repository;

import com.gamercommunity.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //Refresh Token으로 조회
    Optional<RefreshToken> findByRefreshToken(String refreshToken);


}
