package com.sparta.purchaseservice.connector;

import com.sparta.purchaseservice.dto.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ProductConnection {

    private final WebClient webClient;

    public ProductConnection(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8060").build();
    }

    public Flux<Product> getFFProduct() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/live/stock")
                        .build())
                .retrieve()
                .bodyToFlux(Product.class) // Flux<Product>로 응답 수신
                .map(product -> new Product(
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductDescription(),
                        product.getProductPrice(),
                        product.getStockQuantity()
                ));
    }

//    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCancelProduct")
//    @TimeLimiter(name = "productService")
//    @Retry(name = "productService")
    public CompletableFuture<Void> cancelProduct() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/cancel")
                        .queryParam("productName", "선착순")
                        .queryParam("orderQuantity", 1)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }

    public CompletableFuture<Void> fallbackCancelProduct(String productName,int cancelQuantity, Throwable throwable) {
        return CompletableFuture.runAsync(() -> {});
    }
}
