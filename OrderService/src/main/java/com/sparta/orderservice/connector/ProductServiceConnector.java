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
        this.webClient = webClientBuilder.baseUrl("http://product-service:8060").build();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackIsProductExist")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
    public boolean isProductExist(String productName) {
        log.info("찾으려는 상품 이름 : " + productName);
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
    public boolean fallbackIsProductExist(String productName, Throwable throwable) {
        throw new CustomException(productName + ". 상품 주문 실패<br>원인 : " + throwable.getMessage()+"<br>원인 : 상품이 존재하지 않음");
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackExistByProductNameAndOverQuantity")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
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
    // Fallback 메서드
    public boolean fallbackExistByProductNameAndOverQuantity(String productName, Throwable throwable) {
        throw new CustomException(productName + "상품 주문 실패\n원인 : " + throwable.getMessage()+"\n원인 : 상품 재고 부족");
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackOrderProduct")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
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
    // Fallback 메서드
    public void fallbackOrderProduct(String productName, Throwable throwable) {
        log.error("Fallback executed due to: {}", throwable.getMessage());
        throw new CustomException("Failed to order product: " + productName + ". Reason: " + throwable.getMessage());
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCancelProduct")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
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
    // Fallback 메서드
    public void fallbackCancelProduct(String productName, Throwable throwable) {
        log.error("Fallback executed due to: {}", throwable.getMessage());
        throw new CustomException("Failed to order product: " + productName + ". Reason: " + throwable.getMessage());
    }
}
