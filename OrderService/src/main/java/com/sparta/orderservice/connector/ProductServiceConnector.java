package com.sparta.orderservice.connector;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@Component
public class ProductServiceConnector {

    private final WebClient webClient;

    public ProductServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8222").build();
    }

    public boolean isProductExistAndQuantityIsOverOrderQuantity(String productName, int orderQuantity) {
        try {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/isExist")
                            .queryParam("token", productName)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(isValid);

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
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        Mono.error(new RuntimeException("API 호출 실패"))
                )
                .bodyToMono(Void.class)
                .block();



    }
}
