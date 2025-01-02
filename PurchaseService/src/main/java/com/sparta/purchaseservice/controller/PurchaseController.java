package com.sparta.purchaseservice.controller;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/purchase")
@RestController
public class PurchaseController {

    private final ProductConnection productConnection;
    private final OrderConnection orderConnection;
    private final AtomicBoolean apiActive = new AtomicBoolean(false);

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("선착순 구매 서비스 정상 확인.");
    }

    @Scheduled(fixedRate = 3000) // 매 분마다 상태 갱신
    public void updateApiStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 3, 0, 7); // 예: 시작시간 9:00
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 3, 0, 22); // 예: 종료시간 18:00
        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            apiActive.set(true);
        } else {
            apiActive.set(false);
        }
    }

    @Operation(summary = "상품 재고 실시간 조회", description = "특정 상품의 남은 재고 수량을 조회합니다.")
    @GetMapping("/live/stock")
    public Mono<ResponseEntity<Integer>> getRemainingStock(@RequestHeader("Authorization") String token) {
//        if (!apiActive.get()) {
//            return Mono.just(ResponseEntity.status(403).build()); // API 비활성화 시 403 반환
//        }

        return productConnection.getRemainingStock()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

//    @Operation(summary = "결제 프로세스 시작", description = "주문을 위한 결제 프로세스를 시작합니다.")
//    @PostMapping("/{productId}/start")
//    public Mono<ResponseEntity<String>> startPaymentProcess(@RequestHeader("Authorization") String token) {
//        return orderConnection.startPayment(productId, request)
//                .map(order -> ResponseEntity.ok(order))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @Operation(summary = "결제 완료 처리", description = "결제 요청을 처리하고 주문을 완료합니다.")
//    @PostMapping("/{orderId}/complete")
//    public Mono<ResponseEntity<Order>> completePayment(@RequestHeader("Authorization") String token) {
//        return orderConnection.completePayment(orderId)
//                .map(order -> ResponseEntity.ok(order))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @Operation(summary = "주문 정보 조회", description = "특정 주문의 상세 정보를 조회합니다.")
//    @GetMapping("/{orderId}")
//    public Mono<ResponseEntity<Order>> getOrderDetails(@RequestHeader("Authorization") String token) {
//        return orderConnection.getOrderDetails(orderId)
//                .map(order -> ResponseEntity.ok(order))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
}
