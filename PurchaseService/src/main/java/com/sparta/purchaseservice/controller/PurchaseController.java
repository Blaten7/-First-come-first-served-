package com.sparta.purchaseservice.controller;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/purchase")
@RestController
public class PurchaseController {

    private final ProductConnection productConnection;
    private final OrderConnection orderConnection;
    private boolean apiActive = false; // 현재 상태를 저장
    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2025, 1, 3, 2, 25, 0, 0, ZoneId.of("Asia/Seoul"));
    private static final ZonedDateTime END_TIME = ZonedDateTime.of(2025, 1, 3, 2, 26, 0, 0, ZoneId.of("Asia/Seoul"));

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("선착순 구매 서비스 정상 확인.");
    }


    @Scheduled(fixedRate = 3000, initialDelay = 3000) // 3초마다 실행
    public void updateApiStatus() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        boolean newStatus = now.isAfter(START_TIME) && now.isBefore(END_TIME);

        // 상태가 변경된 경우에만 동작 수행
        if (apiActive != newStatus) {
            apiActive = newStatus;
            if (apiActive) {
                onActivation();
            } else {
                onDeactivation();
            }
        }
    }

    private void onActivation() {
        System.out.println("API 활성화 상태로 변경되었습니다. 현재 시간: " + ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
//        return productConnection.getRemainingStock()
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private void onDeactivation() {
        System.out.println("API 비활성화 상태로 변경되었습니다. 현재 시간: " + ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
    }

//    @Operation(summary = "상품 재고 실시간 조회", description = "특정 상품의 남은 재고 수량을 조회합니다.")
//    @GetMapping("/live/stock")
//    public Mono<ResponseEntity<Integer>> getRemainingStock(@RequestHeader("Authorization") String token) {
//        if (!apiActive) {
//            return Mono.just(ResponseEntity.status(403).build()); // API 비활성화 시 403 반환
//        }
//
//        return productConnection.getRemainingStock()
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }

//    @Operation(summary = "결제 프로세스 시작", description = "주문을 위한 결제 프로세스를 시작합니다.")
//    @PostMapping("/{productId}/start")
//    public Mono<ResponseEntity<String>> startPaymentProcess(@RequestHeader("Authorization") String token) {
//        return orderConnection.startPayment(productId, request)
//                .map(order -> ResponseEntity.ok(order))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }

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
