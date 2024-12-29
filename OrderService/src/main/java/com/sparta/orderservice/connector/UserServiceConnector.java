package com.sparta.orderservice.connector;

import com.sparta.orderservice.handler.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class UserServiceConnector {

    private final WebClient webClient;

    public UserServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8050").build();
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackIsValidToken")
    @TimeLimiter(name = "userService")
    @Retry(name = "userService")
    public boolean isValidToken(String token) {
        try {
            // API 호출
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/isValid")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(); // 블로킹 방식으로 결과 대기 (필요 시 비동기로 변경 가능)

            return !Boolean.TRUE.equals(isValid);

        } catch (Exception e) {
            throw new RuntimeException("Error during product existence check", e);
        }
    }
    public void fallbackIsValidToken(String token, Throwable throwable) {
        log.error("Fallback executed due to: {}", throwable.getMessage());
        throw new CustomException("Failed to confirm token: " + token + ". Reason: " + throwable.getMessage());
    }

}
