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
public class ProductServiceConnector {

    private final WebClient webClient;

    public ProductServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8060").build();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackIsProductExist")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
    public boolean isProductExist(String productName) {
        try {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/isExist")
                            .queryParam("productName", productName)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return !Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            throw new RuntimeException("Error during product existence check", e);
        }
    }

    // Fallback 메서드
    public void fallbackIsProductExist(String productName, Throwable throwable) {
        log.error("Fallback executed due to: {}", throwable.getMessage());
        throw new CustomException("Failed to order product: " + productName + ". Reason: " + throwable.getMessage());
    }

    public boolean existByProductNameAndOverQuantity(String productName, int orderQuantity) {
        try {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/isOverQuantity")
                            .queryParam("productName", productName)
                            .queryParam("orderQuantity", orderQuantity)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return !Boolean.TRUE.equals(isValid);

        } catch (WebClientResponseException e) {
            System.err.println("Error response: " + e.getStatusCode());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return false;
        }
    }

    public void orderProduct(String productName, Integer orderQuantity) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/order")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", orderQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void cancelProduct(String productName, int cancelQuantity) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/cancel")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", cancelQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
