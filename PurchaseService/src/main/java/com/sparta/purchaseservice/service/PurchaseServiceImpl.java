package com.sparta.purchaseservice.service;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final ProductConnection productConnection;
    private final OrderConnection orderConnection;

    @Override
    public Mono<ResponseEntity<String>> startPaymentProcess(String token) {
        boolean isAbandoned = Math.random() < 0.2; // 20% 확률로 이탈
        if (isAbandoned) {
            return productConnection.cancelProduct()
                    .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("고객이 결제 시도 중 이탈했습니다.")));
        } else {
            return completePayment(token);
        }
    }

    @Override
    public Mono<ResponseEntity<String>> completePayment(String token) {
        boolean isAbandoned = Math.random() < 0.2; // 20% 확률로 이탈
        if (isAbandoned) {
            return productConnection.cancelProduct()
                    .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("고객이 결제 중 이탈했습니다.")));
        } else {
            orderConnection.completePayment(token);
            return Mono.just(ResponseEntity.ok().body("결제가 완료되었습니다."));
        }
    }
}
