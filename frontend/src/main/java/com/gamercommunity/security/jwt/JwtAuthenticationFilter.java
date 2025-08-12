package com.gamercommunity.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    //인증필요없는 경로
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/users/login")
                || path.startsWith("/api/users/join")
                || path.startsWith("/api/users/check-id")
                || path.startsWith("/api/users/check-nickname")
                || path.startsWith("/api/auth/reissue");
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 있고 유효한 경우에만 인증 정보 설정
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                String loginId = jwtTokenProvider.getLoginIdFromToken(token);

                // SecurityContext에 인증 객체 등록 (기본 ROLE_USER 권한 부여)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다.", loginId);
            } catch (Exception e) {
                log.error("Could not set user authentication in security context", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
