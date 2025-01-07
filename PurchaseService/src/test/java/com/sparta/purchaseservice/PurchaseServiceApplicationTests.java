package com.sparta.purchaseservice;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.controller.PurchaseController;
import com.sparta.purchaseservice.service.PurchaseService;
import io.lettuce.core.RedisConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PurchaseController.class)
class PurchaseServiceApplicationTests {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private OrderConnection orderConnection;

    @Autowired
    private WebTestClient webTestClient;

    private static final String TEST_TOKEN = "test-token";
    private static final String LOCK_KEY = "purchase_lock:test-token";
    private static final String STOCK_KEY = "product_stock";
    private final PurchaseController purchaseController;

    PurchaseServiceApplicationTests(PurchaseController purchaseController) {
        this.purchaseController = purchaseController;
    }


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(purchaseController, "apiActive", true);
        ReflectionTestUtils.setField(purchaseController, "LOCK_TIMEOUT", 30000L);
    }

    @Test
    @DisplayName("결제 프로세스 시작 - 성공")
    void startPaymentProcess_Success() {
        // Given
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L); // 락 획득 성공

        when(purchaseService.startPaymentProcess(anyString()))
                .thenReturn(Mono.just(ResponseEntity.ok("결제가 시작되었습니다.")));

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("결제가 시작되었습니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 - API 비활성화")
    void startPaymentProcess_ApiInactive() {
        // Given
        ReflectionTestUtils.setField(purchaseController, "apiActive", false);

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class)
                .isEqualTo("서비스가 현재 비활성화 상태입니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 - 락 획득 실패")
    void startPaymentProcess_LockAcquisitionFailed() {
        // Given
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(0L); // 락 획득 실패

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody(String.class)
                .isEqualTo("이미 진행 중인 결제가 있습니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 - 재고 부족")
    void startPaymentProcess_OutOfStock() {
        // Given
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L) // 락 획득 성공
                .thenReturn(0L); // 재고 감소 실패

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(String.class)
                .isEqualTo("재고가 부족합니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 - 결제 처리 시간 초과")
    void startPaymentProcess_Timeout() {
        // Given
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L); // 락 획득 성공

        when(purchaseService.startPaymentProcess(anyString()))
                .thenReturn(Mono.error(new TimeoutException()));

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(String.class)
                .isEqualTo("결제 처리 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 - Redis 오류")
    void startPaymentProcess_RedisError() {
        // Given
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenThrow(new RedisConnectionException("Redis connection failed"));

        // When & Then
        webTestClient.post()
                .uri("/api/purchase/live/product/start")
                .header("Authorization", TEST_TOKEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(String.class)
                .isEqualTo("시스템 오류가 발생했습니다.");
    }
}