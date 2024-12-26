package com.sparta.userservice.config;

import com.sparta.userservice.component.JwtAuthenticationFilter;
import com.sparta.userservice.component.JwtAuthenticationWebFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@AllArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean(name = "serverHttpSecurity")
    @Primary
    public ServerHttpSecurity serverHttpSecurity() {
        return ServerHttpSecurity.http();
    }

//    @Bean
//    public SecurityWebFilterChain securityFilterChain(@Qualifier("serverHttpSecurity") ServerHttpSecurity http) {
//        return http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 비활성화
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/api/user/signup", "/api/user/auth/verify",
//                                "/api/user/isValid", "/api/user/isValid/token", "/api/user/login", "/delete/ALL").permitAll() // 인증 없이 접근 허용
//                        .anyExchange().authenticated() // 나머지 요청은 인증 필요
//                )
////                .logout(ServerHttpSecurity.LogoutSpec::disable) // 로그아웃 비활성화
//                .addFilterAt(new JwtAuthenticationWebFilter(jwtAuthenticationFilter), SecurityWebFiltersOrder.AUTHENTICATION) // JWT 인증 필터 추가
//                .build();
//    }
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/user/signup", "/api/user/auth/verify", "/api/user/login").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationWebFilter(jwtAuthenticationFilter), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화에 BCrypt 사용
    }
}
