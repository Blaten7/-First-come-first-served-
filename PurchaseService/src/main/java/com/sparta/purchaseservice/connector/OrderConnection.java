package com.sparta.purchaseservice.connector;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OrderConnection {

    private final WebClient webClient;

    public OrderConnection(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8070").build();
    }


    public void startPayment(String token) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order/purchase/start")
                        .queryParam("token", token)
                        .build())
                .retrieve() // 응답을 처리하기 위한 메서드
                .bodyToMono(String.class) // 응답 본문을 String으로 매핑
                .doOnSuccess(response -> {
                    System.out.println("정상 실행");
                })
                .doOnError(error -> {
                    System.err.println("에러 발생: " + error.getMessage());
                })
                .subscribe(); // 실제 요청 실행
    }


    public void completePayment(String token) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order/purchase/end")
                        .queryParam("token", token)
                        .build())
                .retrieve() // 응답을 처리하기 위한 메서드
                .bodyToMono(String.class) // 응답 본문을 String으로 매핑
                .doOnSuccess(response -> {
                    System.out.println("정상 실행");
                })
                .doOnError(error -> {
                    System.err.println("에러 발생: " + error.getMessage());
                })
                .subscribe(); // 실제 요청 실행
    }

}
