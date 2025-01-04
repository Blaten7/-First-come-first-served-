package com.sparta.purchaseservice.controller;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import com.sparta.purchaseservice.dto.Product;
import com.sparta.purchaseservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/purchase")
@RestController
public class PurchaseController {

    private final ProductConnection productConnection;
    private final OrderConnection orderConnection;
    private final PurchaseService purchaseService;

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PURCHASE_LOCK_KEY = "purchase_lock:";
    private static final long LOCK_TIMEOUT = 10000; // 10 seconds

    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2025, 1, 4, 0, 25, 0, 0, ZoneId.of("Asia/Seoul"));
    private static final ZonedDateTime END_TIME = ZonedDateTime.of(2025, 1, 4, 23, 26, 0, 0, ZoneId.of("Asia/Seoul"));
    private boolean apiActive = false; // 현재 상태를 저장

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

    @Operation(summary = "상품 재고 실시간 조회", description = "특정 상품들의 상세 정보와 남은 재고 수량을 조회합니다.")
    @GetMapping("/live/stock")
    public Mono<ResponseEntity<String>> getRemainingStock(@RequestHeader("Authorization") String token) {
        if (!apiActive) {
            return Mono.just(ResponseEntity.status(403).build()); // API 비활성화 시 403 반환
        }
        return productConnection.getFFProduct()
                .collectList()
                .map(products -> {
                    StringBuilder htmlResponse = new StringBuilder();
                    htmlResponse.append("<html><body><h1>선착순 구매상품 리스트</h1><ul>");
                    for (Product product : products) {
                        htmlResponse.append("<li>")
                                .append(product.getProductName())
                                .append(" - <a href='http://localhost:8222/api/order/product/주문")
                                .append("'>주문</a></li>")
                                .append("<br>상품 설명 : ").append(product.getProductDescription())
                                .append("<br>남은 재고 : ").append(product.getStockQuantity());
                    }
                    htmlResponse.append("</ul></body></html>");
                    return ResponseEntity.ok(htmlResponse.toString());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "결제 프로세스 시작", description = "주문을 위한 결제 프로세스를 시작합니다.")
    @PostMapping("/live/product/start")
    public Mono<ResponseEntity<String>> startPaymentProcess(@RequestHeader("Authorization") String token) {
        log.info("결제 프로세스 시작");
        if (!apiActive) {
            return Mono.just(ResponseEntity.status(403).build());
        }

        String lockKey = PURCHASE_LOCK_KEY + token;

        return Mono.fromCallable(() -> {
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "locked", Duration.ofMillis(LOCK_TIMEOUT));

            if (Boolean.TRUE.equals(locked)) {
                try {
                    orderConnection.startPayment(token);
                    return purchaseService.startPaymentProcess(token).block();
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                return ResponseEntity.status(429)
                        .body("이미 진행 중인 결제가 있습니다. 잠시 후 다시 시도해주세요.");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
