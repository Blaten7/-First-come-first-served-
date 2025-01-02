package com.sparta.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        // CSRF 비활성화
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        // CORS 설정
        http.cors(cors -> cors.configurationSource(request -> {
            var config = new org.springframework.web.cors.CorsConfiguration();
            config.setAllowedOrigins(List.of("*")); // 모든 Origin 허용
            config.setAllowedMethods(List.of("*")); // 모든 HTTP Method 허용
            config.setAllowedHeaders(List.of("Authorization", "Content-Type")); // 허용할 헤더
            config.setExposedHeaders(List.of("Authorization")); // 노출할 헤더
            return config;
        }));

        // 인증/인가 설정
        http.authorizeExchange(exchange -> exchange
                .anyExchange().permitAll() // 모든 요청 허용
        );

        return http.build();
    }
}
