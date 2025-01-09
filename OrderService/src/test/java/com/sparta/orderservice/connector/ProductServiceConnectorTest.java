package com.sparta.orderservice.connector;

import com.sparta.orderservice.dto.Product;
import com.sparta.orderservice.handler.CustomException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.netty.handler.timeout.TimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceConnectorTest {

    private MockWebServer mockWebServer;
    private ProductServiceConnector productServiceConnector;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://%s:%s", mockWebServer.getHostName(), mockWebServer.getPort());
        productServiceConnector = new ProductServiceConnector(WebClient.builder().baseUrl(baseUrl));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    @Test
    @DisplayName("상품 재고 확인 성공")
    void findByProductNameAndOverQuantitySuccess() throws InterruptedException {
        String productName = "testProduct";
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("[{\"productName\":\"testProduct\",\"stockQuantity\":10}]")
                        .addHeader("Content-Type", "application/json")
        );

        // when & then
        StepVerifier.create(productServiceConnector.findByProductNameAndOverQuantity("testProduct"))
                .assertNext(product -> {
                    assertThat(product.getProductName()).isEqualTo("testProduct");
                    assertThat(product.getStockQuantity()).isEqualTo(10);
                })
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("주문 성공")
    void orderProductSuccess() throws Exception {
        // given
        String productName = "testProduct";
        int orderQuantity = 1;
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
        );

        // when
        CompletableFuture<Void> future = productServiceConnector.orderProduct(productName, orderQuantity);

        // then
        future.get();  // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelProductSuccess() throws Exception {
        // given
        String productName = "testProduct";
        int cancelQuantity = 1;
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
        );

        // when
        CompletableFuture<Void> future = productServiceConnector.cancelProduct(productName, cancelQuantity);

        // then
        future.get();  // 예외가 발생하지 않으면 성공
    }


    //    @Test
//    void testProxyObject() {
//        System.out.println("빈 클래스: " + productServiceConnector.getClass().getName());
//    }
//
//    @Test
//    void testCircuitBreakerManually() {
//        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
//        CircuitBreaker circuitBreaker = registry.circuitBreaker("productService");
//
//        System.out.println("서킷 브레이커 상태: " + circuitBreaker.getState());
//    }
//
//    @Test
//    void testCircuitBreakerManuallyWithError() {
//        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
//        CircuitBreaker circuitBreaker = registry.circuitBreaker("productService");
//
//        System.out.println("초기 서킷 브레이커 상태: " + circuitBreaker.getState());
//
//        // 에러를 발생시켜 상태 변경
//        for (int i = 0; i < 10; i++) {
//            // 에러 발생 시간 0ms로 설정 (테스트 목적)
//            circuitBreaker.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("테스트 실패"));
//        }
//
//        System.out.println("에러 발생 후 서킷 브레이커 상태: " + circuitBreaker.getState());
//    }
//
//    @Test
//    @DisplayName("서비스 중지시 서킷브레이커 동작 테스트")
//    void circuitBreakerTest() {
//        // given
//        String productName = "testProduct";
//
//        // when & then
//        StepVerifier.create(productServiceConnector.findByProductNameAndOverQuantity(productName))
//                .expectNext(new Product("fallbackProduct", 0))
//                .verifyComplete();
//    }
//    @Test
//    @DisplayName("상품 재고 확인 실패 - 서버 에러")
//    void findByProductNameAndOverQuantityServerError() {
//        String productName = "testProduct";
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setResponseCode(500)
//                        .setHeader("Content-Type", "application/json")
//                        .setBody("{\"message\": \"Internal Server Error\"}"));  // 에러 응답 본문 추가
//
//        StepVerifier.create(productServiceConnector.findByProductNameAndOverQuantity(productName))
//                .expectErrorMatches(throwable -> throwable instanceof CustomException &&
//                        throwable.getMessage().contains(productName) &&
//                        throwable.getMessage().contains("Connection refused"))
//                .verify();
//    }
//    @Test
//    @DisplayName("서버 비활성 상태에서 Fallback 메서드 호출")
//    void fallbackIsCalledWhenServerIsDown() {
//        // given
//        String productName = "testProduct";
//
//        // 서버가 꺼진 상태를 가정하므로 MockWebServer를 설정하지 않습니다.
//
//        // when & then
//        StepVerifier.create(productServiceConnector.findByProductNameAndOverQuantity(productName))
//                .expectErrorMatches(throwable ->
//                        throwable instanceof CustomException &&
//                                throwable.getMessage().contains(productName) &&
//                                throwable.getMessage().contains("상품 재고 부족")
//                )
//                .verify();
//    }
//    @Test
//    @DisplayName("주문 실패 - 서버 에러")
//    void orderProductServerError() {
//        // given
//        String productName = "testProduct";
//        int orderQuantity = 1;
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setResponseCode(500)
//                        .addHeader("Content-Type", "application/json")
//        );
//
//        // when & then
//        CompletableFuture<Void> future = productServiceConnector.orderProduct(productName, orderQuantity);
//        assertThatThrownBy(future::get)
//                .hasCauseInstanceOf(WebClientRequestException.class)
//                .hasMessageContaining("Connection refused");
//    }
//    @Test
//    @DisplayName("주문 취소 실패 - 서버 에러")
//    void cancelProductServerError() {
//        // given
//        String productName = "testProduct";
//        int cancelQuantity = 1;
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setResponseCode(500)
//                        .addHeader("Content-Type", "application/json")
//        );
//
//        // when & then
//        CompletableFuture<Void> future = productServiceConnector.cancelProduct(productName, cancelQuantity);
//        assertThatThrownBy(future::get)
//                .hasCauseInstanceOf(CustomException.class)
//                .hasMessageContaining("Failed to cancel product");
//    }
//
//    @Test
//    @DisplayName("타임아웃 테스트")
//    void timeoutTest() {
//        // given
//        String productName = "testProduct";
//        int orderQuantity = 1;
//
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setSocketPolicy(SocketPolicy.NO_RESPONSE)
//        );
//
//        // when & then
//        CompletableFuture<Void> future = productServiceConnector.orderProduct(productName, orderQuantity);
//
//        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
//                .isInstanceOf(TimeoutException.class);
//    }
}