package com.sparta.purchaseservice.service;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import com.sparta.purchaseservice.exception.PaymentProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final ProductConnection productConnection;
    private final OrderConnection orderConnection;

    @Override
    public Mono<ResponseEntity<String>> startPaymentProcess(String token) {
        return productConnection.getFFProduct()
                .next()  // 첫 번째 상품 선택
                .flatMap(product -> {
                    boolean isAbandoned = Math.random() < 0.2;
                    if (isAbandoned) {
                        return productConnection.cancelProduct()
                                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("고객이 결제 시도 중 이탈했습니다")));
                    }

                    // OrderConnection의 startPayment를 Mono로 래핑
                    return Mono.fromRunnable(() -> orderConnection.startPayment(token))
                            .then(completePayment(token));
                })
                .onErrorResume(PaymentProcessingException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(e.getMessage()))
                )
                .onErrorResume(e -> {
                    log.error("결제 처리 중 오류 발생", e);
                    return productConnection.cancelProduct()
                            .then(Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("결제 처리 과정에서 오류가 발생했습니다.")));
                });
    }

    @Override
    public Mono<ResponseEntity<String>> completePayment(String token) {
        boolean isAbandoned = Math.random() < 0.2;
        if (isAbandoned) {
            return productConnection.cancelProduct()
                    .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("고객이 결제 중 이탈했습니다")));
        }

        // OrderConnection의 completePayment를 Mono로 래핑
        return Mono.fromRunnable(() -> orderConnection.completePayment(token))
                .thenReturn(ResponseEntity.ok().body("결제가 완료되었습니다."));
    }
}
