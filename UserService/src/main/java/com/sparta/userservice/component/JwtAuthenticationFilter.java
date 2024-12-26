package com.sparta.userservice.component;

import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final RedisTokenRepository redisTokenRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 제거

            return redisTokenRepository.isBlacklisted(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("블랙리스트에 포함된 토큰: {}", token);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return jwtUtil.isTokenValid(token)
                                .flatMap(isValid -> {
                                    if (isValid) {
                                        log.info("토큰이 유효합니다. 인증 정보를 생성합니다.");
                                        Authentication authentication = jwtUtil.getAuthentication(token);

                                        SecurityContext securityContext = new SecurityContextImpl(authentication);
                                        log.info("Security Context 설정 중: {}", securityContext.getAuthentication());

                                        return chain.filter(exchange)
                                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                                                .doOnSuccess(unused -> log.info("Security Context 설정 완료: {}", securityContext.getAuthentication()));
                                    } else {
                                        log.warn("유효하지 않은 토큰: {}", token);
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    }
                                });
                    });
        }

        log.warn("Authorization 헤더가 없습니다.");
        return chain.filter(exchange);
    }


    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString(); // JWT 토큰 가져오기

        return jwtUtil.isTokenValid(token)
                .flatMap(isValid -> {
                    if (isValid) {
                        log.info("토큰이 유효합니다. 인증 정보를 생성합니다."); // 추가된 로그
                        return Mono.just(jwtUtil.getAuthentication(token));
                    } else {
                        log.warn("유효하지 않은 토큰: {}", token); // 추가된 로그
                        return Mono.empty(); // 빈 Mono 반환
                    }
                })
                .doOnError(e -> log.error("토큰 검증 중 오류 발생: {}", e.getMessage(), e)); // 오류 로그 추가
    }

    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization")) // Authorization 헤더 가져오기
                .filter(authHeader -> true) // "Bearer "로 시작하는지 확인
                .map(authHeader -> authHeader.substring(7)) // "Bearer " 이후의 토큰 부분 추출
                .flatMap(token -> jwtUtil.isTokenValid(token)
                        .filter(isValid -> isValid) // 토큰 유효성 검증
                        .map(isValid -> jwtUtil.getAuthentication(token)) // 인증 정보 생성 및 반환
                );
    }


}
