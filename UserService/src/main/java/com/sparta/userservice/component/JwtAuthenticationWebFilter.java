package com.sparta.userservice.component;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

    public JwtAuthenticationWebFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        super(new ReactiveAuthenticationManager() {
            @Override
            public Mono authenticate(Authentication authentication) {
                return jwtAuthenticationFilter.authenticate(authentication);
            }
        });

        this.setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono convert(ServerWebExchange exchange) {
                return jwtAuthenticationFilter.convert(exchange);
            }
        });
    }
}
