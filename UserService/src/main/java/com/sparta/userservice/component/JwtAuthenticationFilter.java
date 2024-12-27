package com.sparta.userservice.component;

import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTokenRepository redisTokenRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisTokenRepository redisTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.redisTokenRepository = redisTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 블랙리스트 토큰 확인
                if (redisTokenRepository.isBlacklisted(token)) {
                    log.warn("블랙리스트에 포함된 토큰: {}", token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // JWT 유효성 확인
                if (jwtUtil.isTokenValid(token)) {
                    log.info("토큰이 유효합니다. 인증 정보를 생성합니다.");
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Security Context 설정 완료: {}", authentication);
                } else {
                    log.warn("유효하지 않은 토큰: {}", token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("토큰 처리 중 오류 발생: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        } else {
            log.warn("Authorization 헤더가 없습니다.");
        }

        filterChain.doFilter(request, response);
    }

//    public Authentication authenticate(Authentication authentication) {
//        String token = authentication.getCredentials().toString(); // JWT 토큰 가져오기
//
//        try {
//            if (jwtUtil.isTokenValid(token)) {
//                log.info("토큰이 유효합니다. 인증 정보를 생성합니다.");
//                return jwtUtil.getAuthentication(token);
//            } else {
//                log.warn("유효하지 않은 토큰: {}", token);
//                return null;
//            }
//        } catch (Exception e) {
//            log.error("토큰 검증 중 오류 발생: {}", e.getMessage(), e);
//            return null;
//        }
//    }
//
//    public Authentication convert(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization"); // Authorization 헤더 가져오기
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7); // "Bearer " 이후의 토큰 부분 추출
//
//            try {
//                if (jwtUtil.isTokenValid(token)) {
//                    return jwtUtil.getAuthentication(token);
//                } else {
//                    log.warn("유효하지 않은 토큰: {}", token);
//                }
//            } catch (Exception e) {
//                log.error("토큰 검증 중 오류 발생: {}", e.getMessage(), e);
//            }
//        } else {
//            log.warn("Authorization 헤더가 없습니다.");
//        }
//
//        return null;
//    }
}
