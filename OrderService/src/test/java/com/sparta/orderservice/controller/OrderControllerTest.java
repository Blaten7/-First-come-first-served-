package com.sparta.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.orderservice.connector.ProductServiceConnector;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.Product;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.WishlistRepository;
import com.sparta.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Mock
    private ProductServiceConnector productServiceConnector;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    private final String AUTH_TOKEN = "Bearer test-token";

    @BeforeEach
    void setUp() {
        // Mock 설정
        when(orderService.extractEmail(anyString())).thenReturn("test@email.com");
    }

//    @Test
//    @DisplayName("상품 주문 성공")
//    void orderProductSuccess() {
//        // given
//        OrderRequestDto request = new OrderRequestDto();
//        request.setProductId(1L);
//
//        // when & then
//        webTestClient.post()
//                .uri("/api/order/product")
//                .header("Authorization", AUTH_TOKEN)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk();
//    }

    @Test
    @DisplayName("상품 주문 성공")
    void orderProductSuccess() throws Exception {
        // Given
        OrderRequestDto request = new OrderRequestDto();
        request.setProductName("테스트상품");
        request.setStockQuantity(1);

        Product product = new Product();
        product.setStockQuantity(10);

        given(productServiceConnector.findByProductNameAndOverQuantity("테스트상품"))
                .willReturn(Flux.just(product));
        given(orderService.extractEmail(anyString())).willReturn("test@email.com");

        // When & Then
        mockMvc.perform(post("/api/order/product/주문")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("상품 주문이 완료되었습니다."));
    }

    @Test
    @DisplayName("주문 실패 - 재고 부족")
    void orderProductFailStockNotEnough() {
        // Given
        OrderRequestDto request = new OrderRequestDto();
        request.setProductName("테스트상품");
        request.setStockQuantity(100);

        Product product = new Product();
        product.setStockQuantity(1);

        given(productServiceConnector.findByProductNameAndOverQuantity("테스트상품"))
                .willReturn(Flux.just(product));

        // When & Then
        webTestClient.post()
                .uri("/api/order/product/주문")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(String.class)
                .isEqualTo("상품 재고가 주문 수량보다 적습니다.");
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrderSuccess() {
        // Given
        String productName = "테스트상품";
        given(orderService.extractEmail(anyString())).willReturn("test@email.com");
        when(orderRepository.findTotalAmountByUserEmailAndOrderStatusAndProductName(
                anyString(), anyString(), anyString())).thenReturn(1);

        // When & Then
        webTestClient.patch()
                .uri("/api/order/cancel/{productName}", productName)
                .header("Authorization", AUTH_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("주문이 취소되었습니다.");
    }

    @Test
    @DisplayName("위시리스트 추가 성공")
    void addToWishlistSuccess() {
        // Given
        OrderRequestDto request = new OrderRequestDto();
        request.setProductName("테스트상품");
        request.setStockQuantity(1);

        Product product = new Product();
        product.setStockQuantity(10);

        given(productServiceConnector.findByProductNameAndOverQuantity("테스트상품"))
                .willReturn(Flux.just(product));
        given(orderService.extractEmail(anyString())).willReturn("test@email.com");

        // When & Then
        webTestClient.post()
                .uri("/api/order/product/찜")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("위시리스트에 추가되었습니다.");
    }

    @Test
    @DisplayName("결제 프로세스 시작 성공")
    void purchaseStartSuccess() {
        // Given
        given(orderService.extractEmail(anyString())).willReturn("test@email.com");

        // When & Then
        webTestClient.post()
                .uri("/api/order/purchase/start")
                .header("Authorization", AUTH_TOKEN)
                .exchange()
                .expectStatus().isOk();
    }
}