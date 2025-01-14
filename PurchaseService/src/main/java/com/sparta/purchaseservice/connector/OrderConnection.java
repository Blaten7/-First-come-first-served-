package com.sparta.purchaseservice.connector;

import com.sparta.purchaseservice.exception.PaymentProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrderConnection {
    private final WebClient webClient;

    public OrderConnection(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8070").build();
    }

    public Mono<String> startPayment(String token) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order/purchase/start")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    throw new PaymentProcessingException("결제 시작 중 오류 발생");
                });
    }

    public Mono<String> completePayment(String token) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order/purchase/end")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    throw new PaymentProcessingException("결제 완료 중 오류 발생");
                });
    }
}
