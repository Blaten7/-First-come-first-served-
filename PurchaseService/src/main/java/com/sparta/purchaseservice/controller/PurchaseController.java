package com.sparta.purchaseservice.controller;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import com.sparta.purchaseservice.dto.Product;
import com.sparta.purchaseservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.Collections;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.UUID;

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
    private static final long LOCK_TIMEOUT = 5000; // 10 seconds

    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2025, 1, 4, 0, 25, 0, 0, ZoneId.of("Asia/Seoul"));
    private static final ZonedDateTime END_TIME = ZonedDateTime.of(2025, 5, 4, 23, 26, 0, 0, ZoneId.of("Asia/Seoul"));
    private boolean apiActive = false; // 현재 상태를 저장

    private static final String LOCK_SCRIPT =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "return 1 " +
                    "else " +
                    "return 0 " +
                    "end";

    private final RedisScript<Long> lockScript = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);

    private static final String DECREMENT_STOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "local stock = tonumber(redis.call('get', KEYS[2])) " +
                    "if stock > 0 then " +
                    "redis.call('decr', KEYS[2]) " +
                    "redis.call('del', KEYS[1]) " +
                    "return 1 " +
                    "else " +
                    "return 0 " +
                    "end " +
                    "else " +
                    "return -1 " +
                    "end";

    private final RedisScript<Long> decrementStockScript = new DefaultRedisScript<>(DECREMENT_STOCK_SCRIPT, Long.class);

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

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void syncStockToRedis() {
        productConnection.getFFProduct()
                .collectList()
                .subscribe(products -> {
                    int totalStock = products.stream()
                            .mapToInt(Product::getStockQuantity)
                            .sum();
                    redisTemplate.opsForValue().set("product_stock", String.valueOf(totalStock));
                    log.info("Redis 재고 동기화 완료: " + totalStock);
                });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Operation(summary = "결제 프로세스 시작", description = "주문을 위한 결제 프로세스를 시작합니다.")
    @PostMapping("/live/product/start")
    public Mono<ResponseEntity<String>> startPaymentProcess(@RequestHeader("Authorization") String token) {
        if (!apiActive) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("서비스가 현재 비활성화 상태입니다."));
        }

        String lockKey = PURCHASE_LOCK_KEY + token;
        String lockValue = UUID.randomUUID().toString();

        return Mono.defer(() -> {
            try {
                Long acquired = redisTemplate.execute(lockScript,
                        Collections.singletonList(lockKey),
                        lockValue,
                        String.valueOf(LOCK_TIMEOUT));

                if (acquired != null && acquired == 1L) {
                    return purchaseService.startPaymentProcess(token)
                            .timeout(Duration.ofSeconds(5))
                            .doFinally(signal -> releaseLock(lockKey, lockValue));
                } else {
                    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body("이미 진행 중인 결제가 있습니다."));
                }
            } catch (Exception e) {
                log.error("Lock acquisition error", e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("시스템 오류가 발생했습니다."));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

//    private Mono<ResponseEntity<String>> processPayment(String token, String lockKey, String stockKey, String lockValue) {
//        return Mono.fromCallable(() -> {
//            try {
//                Long decrementResult = redisTemplate.execute(decrementStockScript,
//                        Arrays.asList(lockKey, stockKey),
//                        lockValue);
//
//                if (decrementResult != null && decrementResult == 1) {
//                    return purchaseService.startPaymentProcess(token)
//                            .timeout(Duration.ofSeconds(5))
//                            .block();
//                } else if (decrementResult == 0) {
//                    return ResponseEntity.status(HttpStatus.CONFLICT)
//                            .body("재고가 부족합니다.");
//                } else {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body("재고 처리 중 오류가 발생했습니다.");
//                }
//            } finally {
//                releaseLock(lockKey, lockValue)
//                        .doOnError(e -> log.error("Lock release failed", e)).block();
//            }
//        }).onErrorResume(e -> {
//            log.error("결제 처리 중 오류 발생", e);
//            redisTemplate.opsForValue().increment(stockKey); // Long 타입 반환
//            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body("결제 처리 과정에서 오류가 발생했습니다."))
//                    .onErrorResume(redisError -> {
//                        log.error("Redis 작업 실패", redisError);
//                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                .body("재고 수량 조정 중 오류가 발생했습니다."));
//                    });
//        });
//    }

    private Mono<Void> releaseLock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        return Mono.fromCallable(() ->
                        redisTemplate.execute(
                                new DefaultRedisScript<>(script, Long.class),
                                Collections.singletonList(lockKey),
                                lockValue
                        ))
                .doOnError(e -> log.error("Failed to release lock: {}", lockKey, e))
                .then();
    }
}
