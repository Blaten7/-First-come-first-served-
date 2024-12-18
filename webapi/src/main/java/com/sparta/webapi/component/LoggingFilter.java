package com.sparta.webapi.component;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class LoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("Request URI: " + exchange.getRequest().getURI());
        return chain.filter(exchange);
    }

}
