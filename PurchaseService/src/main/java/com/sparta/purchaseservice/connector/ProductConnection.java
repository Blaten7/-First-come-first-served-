package com.sparta.purchaseservice.connector;

import com.sparta.purchaseservice.dto.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductConnection {

    private final WebClient webClient;

    public ProductConnection(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8060").build();
    }

    public Mono<Integer> getRemainingStock() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/live/stock")
                        .build())
                .retrieve()
                .bodyToFlux(Product.class) // Flux<Product>로 응답 수신
                .map(Product::getStock)    // Product 객체에서 stock 필드 추출
                .reduce(0, Integer::sum);  // 모든 stock 값을 합산하여 Mono<Integer>로 변환
    }

}
