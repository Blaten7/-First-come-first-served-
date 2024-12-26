package com.sparta.userservice.component;

import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final RedisTokenRepository redisTokenRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null) {
            String token = authHeader;

            // 블랙리스트 확인
            return redisTokenRepository.isBlacklisted(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("블랙리스트에 포함된 토큰: {}", token);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        // JWT 유효성 검증
                        return jwtUtil.isTokenValid(token)
                                .flatMap(isValid -> {
                                    if (isValid) {
                                        Authentication authentication = jwtUtil.getAuthentication(token);

                                        // SecurityContext에 인증 정보 설정
                                        return chain.filter(exchange)
                                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                                        Mono.just(new SecurityContextImpl(authentication))));
                                    } else {
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    }
                                });
                    });
        }

        // Authorization 헤더가 없으면 다음 필터로 전달
        return chain.filter(exchange);
    }

    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString(); // JWT 토큰 가져오기

        // 토큰 유효성 검증
        return jwtUtil.isTokenValid(token)
                .flatMap(isValid -> {
                    if (isValid) {
                        // 인증 정보 반환
                        return Mono.just(jwtUtil.getAuthentication(token));
                    }
                    // 유효하지 않은 경우 빈 Mono 반환
                    return Mono.empty();
                });
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
