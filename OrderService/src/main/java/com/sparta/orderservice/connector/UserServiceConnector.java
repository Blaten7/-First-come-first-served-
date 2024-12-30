package com.sparta.orderservice.connector;

import com.sparta.orderservice.handler.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class UserServiceConnector {

    private final WebClient webClient;

    public UserServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://user-service:8050").build();
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackIsValidToken")
    @TimeLimiter(name = "userService")
    @Retry(name = "userService")
    public CompletableFuture<Boolean> isValidToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/isValid")
                            .build())
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(); // 블로킹 방식
            return Boolean.TRUE.equals(isValid);
        });
    }

    public CompletableFuture<Boolean> fallbackIsValidToken(String token, Throwable throwable) {
        log.error("Fallback executed due to: {}", throwable.getMessage());
        return CompletableFuture.completedFuture(false); // 기본값 반환
    }
}
