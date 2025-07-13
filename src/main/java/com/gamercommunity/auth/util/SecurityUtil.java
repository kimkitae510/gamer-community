package com.gamercommunity.auth.util;

import com.gamercommunity.global.exception.custom.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {
    private SecurityUtil() {}


    //토큰인증 유틸
    public static Optional<String> getCurrentLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        String loginId = authentication.getName();

        return Optional.ofNullable(loginId);
    }

    // 로그인 필수인 경우 사용
    public static String getRequiredLoginId() {
        return getCurrentLoginId()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));
    }

}
