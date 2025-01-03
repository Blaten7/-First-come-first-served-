package com.sparta.purchaseservice.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface PurchaseService {

    Mono<ResponseEntity<String>> startPaymentProcess(String token);

    Mono<ResponseEntity<String>> completePayment(String token);
}
