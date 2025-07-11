package com.gamercommunity.global.config;

import com.gamercommunity.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // ========== 인증 불필요 (Public) ==========
                        // 회원가입 & 로그인
                        .requestMatchers("/api/users/join", "/api/users/login", "/api/users/check-**").permitAll()
                        .requestMatchers("/api/auth/reissue").permitAll()

                        // 카테고리 (전체 공개)
                        .requestMatchers("/api/categories/**").permitAll()

                        // 장르 (전체 공개)
                        .requestMatchers("/api/genres/**").permitAll()

                        // 게시글 조회 (GET만 허용)
                        .requestMatchers(HttpMethod.GET, "/api/posts/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/{id}").permitAll()

                        // 댓글 조회 (GET만 허용)
                        .requestMatchers(HttpMethod.GET, "/api/comments/post/**").permitAll()

                        // 리뷰 조회 (GET만 허용)
                        .requestMatchers(HttpMethod.GET, "/api/reviews/game/**").permitAll()

                        // ========== 인증 필요 (Authenticated) ==========
                        // 게시글 작성/수정/삭제
                        .requestMatchers("/api/posts/**").authenticated()

                        // 댓글 작성/수정/삭제
                        .requestMatchers("/api/comments/**").authenticated()

                        // 리뷰 작성/수정/삭제
                        .requestMatchers("/api/reviews/**").authenticated()

                        // 좋아요
                        .requestMatchers("/api/post-likes/**").authenticated()
                        .requestMatchers("/api/comment-likes/**").authenticated()
                        .requestMatchers("/api/review-likes/**").authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
