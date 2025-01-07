package com.sparta.orderservice.controller;

import com.sparta.orderservice.connector.ProductServiceConnector;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.Product;
import com.sparta.orderservice.entity.Order;
import com.sparta.orderservice.entity.Wishlist;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.WishlistRepository;
import com.sparta.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private ProductServiceConnector productServiceConnector;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    private final String TOKEN = "Bearer test-token";
    private final String EMAIL = "test@example.com";

    @Test
    @DisplayName("테스트 엔드포인트 확인")
    void testEndpoint() {
        ResponseEntity<String> response = orderController.test(TOKEN);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("아아 여기는 OrderService 요청 확인!");
    }

    @Nested
    class 주문_API {
        // 성공
        @Test
        @DisplayName("상품 주문 성공")
        void orderProductSuccess() {
            // given
            OrderRequestDto orderRequest = new OrderRequestDto();
            orderRequest.setProductName("testProduct");
            orderRequest.setStockQuantity(1);

            Product product = new Product();
            product.setStockQuantity(10);

            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.order("주문", TOKEN, orderRequest);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isEqualTo("상품 주문이 완료되었습니다.");
                    })
                    .verifyComplete();

            verify(orderRepository).save(any(Order.class));
            verify(productServiceConnector).orderProduct(anyString(), anyInt());
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrderSuccess() {
            // given
            String productName = "testProduct";
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(orderRepository.findTotalAmountByUserEmailAndOrderStatusAndProductName(
                    anyString(), anyString(), anyString())).willReturn(1);

            // when
            ResponseEntity<String> response = orderController.cancelOrder(TOKEN, productName);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("주문이 취소되었습니다.");
            verify(productServiceConnector).cancelProduct(anyString(), anyInt());
        }

        @Test
        @DisplayName("주문 상태 조회 성공")
        void getOrderStatusSuccess() {
            // given
            List<Order> orders = Arrays.asList(new Order(), new Order());
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(orderRepository.findByUserEmail(EMAIL)).willReturn(orders);

            // when
            ResponseEntity<?> response = orderController.getOrderStatus(TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(orders);
        }

        @Test
        @DisplayName("반품 신청 성공")
        void returnOrderSuccess() {
            // given
            String productName = "testProduct";
            Order order = new Order();
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(orderRepository.findByUserEmailAndProductNameAndOrderStatus(
                    anyString(), anyString(), anyString())).willReturn(Optional.of(order));

            // when
            ResponseEntity<String> response = orderController.returnOrder(TOKEN, productName);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("반품 신청이 완료되었습니다");
        }

        // 예외
        @Test
        @DisplayName("잘못된 주문/찜 경로로 요청시 기본 응답")
        void invalidOrderPathTest() {
            // given
            OrderRequestDto orderRequest = new OrderRequestDto();
            orderRequest.setProductName("testProduct");
            orderRequest.setStockQuantity(1);

            Product product = new Product();
            product.setStockQuantity(10);

            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.order("invalid_path", TOKEN, orderRequest);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isEqualTo("주문 로직 정상 실행 완료");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("재고 부족으로 주문 실패")
        void orderFailDueToInsufficientStock() {
            // given
            OrderRequestDto orderRequest = new OrderRequestDto();
            orderRequest.setProductName("testProduct");
            orderRequest.setStockQuantity(10);

            Product product = new Product();
            product.setStockQuantity(5);

            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.order("주문", TOKEN, orderRequest);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(response.getBody()).isEqualTo("상품 재고가 주문 수량보다 적습니다.");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    class 위시리스트_API {
        // 성공
        @Test
        @DisplayName("마이페이지 조회 성공")
        void getMyPageSuccess() {
            // given
            List<Wishlist> wishlists = Arrays.asList(new Wishlist(), new Wishlist());
            List<Order> orders = Arrays.asList(new Order(), new Order());
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(wishlistRepository.findByUserEmail(EMAIL)).willReturn(wishlists);
            given(orderRepository.findByUserEmail(EMAIL)).willReturn(orders);

            // when
            ResponseEntity<Map<String, Object>> response = orderController.getMyPage(TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("wishlist", wishlists);
            assertThat(response.getBody()).containsEntry("orders", orders);
        }

        @Test
        @DisplayName("위시리스트 추가 성공")
        void addToWishlistSuccess() {
            // given
            OrderRequestDto orderRequest = new OrderRequestDto();
            orderRequest.setProductName("testProduct");
            orderRequest.setStockQuantity(1);

            Product product = new Product();
            product.setStockQuantity(10);

            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.order("찜", TOKEN, orderRequest);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isEqualTo("위시리스트에 추가되었습니다.");
                    })
                    .verifyComplete();

            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("위시리스트에서 주문 성공")
        void orderFromWishlistSuccess() {
            // given
            Long wishlistId = 1L;
            Wishlist wishlist = new Wishlist();
            wishlist.setProductName("testProduct");
            wishlist.setQuantity(1);

            Product product = new Product();
            product.setStockQuantity(10);
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.orderFromWishlist(TOKEN, wishlistId);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isEqualTo("위시리스트 상품이 주문되었습니다.");
                    })
                    .verifyComplete();

            verify(wishlistRepository).delete(wishlist);
        }

        @Test
        @DisplayName("위시리스트 조회 성공")
        void getWishlistSuccess() {
            // given
            List<Wishlist> wishlists = Arrays.asList(new Wishlist(), new Wishlist());
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(wishlistRepository.findByUserEmail(EMAIL)).willReturn(wishlists);

            // when
            ResponseEntity<?> response = orderController.getWishlist(TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(wishlists);
        }

        @Test
        @DisplayName("위시리스트 수량 수정 성공")
        void updateWishlistSuccess() {
            // given
            Long wishlistId = 1L;
            int newQuantity = 5;
            given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(new Wishlist()));

            // when
            ResponseEntity<String> response = orderController.updateWishlist(wishlistId, newQuantity, TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("위시리스트 수정이 완료되었습니다.");
        }

        @Test
        @DisplayName("위시리스트 삭제 성공")
        void deleteFromWishlistSuccess() {
            // given
            Long wishlistId = 1L;
            given(wishlistRepository.existsById(wishlistId)).willReturn(true);

            // when
            ResponseEntity<String> response = orderController.deleteFromWishlist(wishlistId, TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("위시리스트 항목이 삭제되었습니다.");
            verify(wishlistRepository).deleteById(wishlistId);
        }
        // 예외
        @Test
        @DisplayName("존재하지 않는 위시리스트에서 주문 시도")
        void orderFromNonExistentWishlistTest() {
            // given
            Long wishlistId = 999L;
            given(wishlistRepository.findById(wishlistId)).willReturn(Optional.empty());

            // when
            Mono<ResponseEntity<String>> result = orderController.orderFromWishlist(TOKEN, wishlistId);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(response.getBody()).isEqualTo("위시리스트에 해당 상품이 존재하지 않습니다.");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("위시리스트 상품 주문시 재고 부족")
        void orderFromWishlistInsufficientStockTest() {
            // given
            Long wishlistId = 1L;
            Wishlist wishlist = new Wishlist();
            wishlist.setProductName("testProduct");
            wishlist.setQuantity(10);

            Product product = new Product();
            product.setStockQuantity(5);

            given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
            given(productServiceConnector.findByProductNameAndOverQuantity(anyString()))
                    .willReturn(Flux.just(product));

            // when
            Mono<ResponseEntity<String>> result = orderController.orderFromWishlist(TOKEN, wishlistId);

            // then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(response.getBody()).isEqualTo("상품 재고가 주문 수량보다 적습니다.");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("빈 위시리스트 조회")
        void getEmptyWishlistTest() {
            // given
            given(orderService.extractEmail(anyString())).willReturn(EMAIL);
            given(wishlistRepository.findByUserEmail(EMAIL)).willReturn(Collections.emptyList());

            // when
            ResponseEntity<?> response = orderController.getWishlist(TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("위시리스트가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 위시리스트 수량 수정 시도")
        void updateNonExistentWishlistTest() {
            // given
            Long wishlistId = 999L;
            int newQuantity = 5;
            given(wishlistRepository.findById(wishlistId)).willReturn(Optional.empty());

            // when
            ResponseEntity<String> response = orderController.updateWishlist(wishlistId, newQuantity, TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("위시리스트 항목을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 위시리스트 삭제 시도")
        void deleteNonExistentWishlistTest() {
            // given
            Long wishlistId = 999L;
            given(wishlistRepository.existsById(wishlistId)).willReturn(false);

            // when
            ResponseEntity<String> response = orderController.deleteFromWishlist(wishlistId, TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("위시리스트 항목을 찾을 수 없습니다.");
            verify(wishlistRepository, never()).deleteById(wishlistId);
        }
    }

    @Test
    @DisplayName("결제 프로세스 시작")
    void purchaseStartSuccess() {
        given(orderService.extractEmail(anyString())).willReturn(EMAIL);
        // when
        orderController.purchaseStart(TOKEN);

        // then
        verify(orderRepository).updateOrderStatusPurchaseStart(EMAIL);
        verify(productServiceConnector).orderProduct("선착순", 1);
    }

    @Test
    @DisplayName("결제 프로세스 완료")
    void purchaseCompleteSuccess() {
        given(orderService.extractEmail(anyString())).willReturn(EMAIL);
        // when
        orderController.purchaseComplete(TOKEN);

        // then
        verify(orderRepository).updateOrderStatusPurchaseComplete(EMAIL);
    }
}