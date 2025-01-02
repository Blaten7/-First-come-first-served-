package com.sparta.orderservice.connector;

import com.sparta.orderservice.dto.Product;
import com.sparta.orderservice.handler.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ProductServiceConnector {

    private final WebClient webClient;

    public ProductServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8060").build();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackFindByProductNameAndOverQuantity")
    @Retry(name = "productService")
    public Flux<Product> findByProductNameAndOverQuantity(String productName) {
        log.info("상품 검증 요청 메서드 진입");
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/isOverQuantity")
                        .queryParam("productName", productName)
                        .build())
                .retrieve()
                .bodyToFlux(Product.class)
                .map(product -> new Product(
                        product.getProductName(),
                        product.getStockQuantity()
                ));
    }

    public Flux<Boolean> fallbackFindByProductNameAndOverQuantity(String productName, Throwable throwable) {
        throw new CustomException(productName + " 상품 주문 실패<br>원인 : " + throwable.getMessage() + "<br>원인 : 상품 재고 부족");
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackOrderProduct")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
    public CompletableFuture<Void> orderProduct(String productName, Integer orderQuantity) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/order")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", orderQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }

    public CompletableFuture<Void> fallbackOrderProduct(String productName, Integer orderQuantity, Throwable throwable) {
        throw new CustomException("Failed to order product: " + productName + ". Reason: " + throwable.getMessage());
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCancelProduct")
    @TimeLimiter(name = "productService")
    @Retry(name = "productService")
    public CompletableFuture<Void> cancelProduct(String productName, int cancelQuantity) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/cancel")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", cancelQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }

    public CompletableFuture<Void> fallbackCancelProduct(String productName, Throwable throwable) {
        throw new CustomException("Failed to cancel product: " + productName + ". Reason: " + throwable.getMessage());
    }

}
