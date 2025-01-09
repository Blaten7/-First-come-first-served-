package com.sparta.purchaseservice.controller;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import com.sparta.purchaseservice.dto.Product;
import com.sparta.purchaseservice.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private ProductConnection productConnection;

    @Mock
    private OrderConnection orderConnection;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PurchaseController purchaseController;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("기본 테스트")
    void testEndpoint() {
        Mono<String> result = purchaseController.test();

        StepVerifier.create(result)
                .expectNext("선착순 구매 서비스 정상 확인.")
                .verifyComplete();
    }

    @Test
    @DisplayName("Redis 재고 동기화 테스트")
    void syncStockToRedisTest() {
        // given
        Product product = new Product(1L, "선착순", "테스트상품", 1000, 10);
        when(productConnection.getFFProduct()).thenReturn(Flux.just(product));

        // when
        purchaseController.syncStockToRedis();

        // then
        verify(valueOperations).set(eq("product_stock"), eq("10"));
    }

    @Test
    @DisplayName("결제 프로세스 시작 테스트")
    void startPaymentProcessTest() {
        // given
        String token = "test-token";
        when(redisTemplate.execute(any(), anyList(), any(), any())).thenReturn(1L);
        when(purchaseService.startPaymentProcess(anyString()))
                .thenReturn(Mono.just(ResponseEntity.ok("결제 시작")));

        // when & then
        StepVerifier.create(purchaseController.startPaymentProcess(token))
                .expectNextMatches(response ->
                        response.getStatusCode().is2xxSuccessful() &&
                                response.getBody().equals("결제 시작"))
                .verifyComplete();
    }
}