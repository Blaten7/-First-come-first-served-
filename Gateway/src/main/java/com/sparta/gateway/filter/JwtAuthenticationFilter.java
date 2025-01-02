package com.sparta.gateway.filter;

import com.sparta.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import reactor.util.context.Context;

import java.net.URI;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // 설정이 필요하다면 여기에 추가 가능
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            URI path = exchange.getRequest().getURI();
            log.info("JwtAuthenticationFilter 실행 - 요청 URI: {}", path);
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if ("/api/user/login".equals(path.getPath())) {
                return chain.filter(exchange.mutate().request(exchange.getRequest()).build());
            }
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("유효하지 않은 토큰");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = authHeader.substring(7); // "Bearer " 이후 토큰 추출


            return Mono.fromCallable(() -> JwtUtil.validateToken(token)) // 비동기적으로 JWT 검증
                    .flatMap(claims -> {
                        if (JwtUtil.isTokenExpired(claims)) {
                            // 토큰이 만료된 경우 401 반환
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        // 검증된 사용자 ID를 Exchange Attribute에 저장
                        return chain.filter(exchange)
                                .contextWrite(Context.of("userEmail", claims.getSubject()));
                    })
                    .onErrorResume(e -> {
                        // 검증 실패 시 401 반환
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
