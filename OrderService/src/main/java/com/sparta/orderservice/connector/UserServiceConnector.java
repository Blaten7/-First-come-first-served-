package com.sparta.orderservice.connector;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class UserServiceConnector {

    private final WebClient webClient;

    public UserServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8050").build();
    }

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

        } catch (WebClientResponseException e) {
            System.err.println("Error response: " + e.getStatusCode());
            return true;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return true;
        }
    }

}
