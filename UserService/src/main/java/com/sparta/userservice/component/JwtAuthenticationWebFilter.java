package com.sparta.userservice.component;

import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

    public JwtAuthenticationWebFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        super(jwtAuthenticationFilter::authenticate);

        this.setServerAuthenticationConverter(jwtAuthenticationFilter::convert);
    }
}
