package com.sparta.webapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Operation(summary = "주문하기", description = "상품을 주문합니다.")
    @PostMapping
    public Map<String, String> placeOrder(@RequestBody Map<String, Object> orderRequest) {
        return Map.of("orderId", "123", "msg", "상품 주문 성공");
    }

    @Operation(summary = "주문 상태 조회", description = "사용자의 주문 상태를 조회합니다.")
    @GetMapping
    public List<Map<String, Object>> getOrderStatus() {
        return List.of(Map.of("orderId", 123, "productName", "상품1", "orderStatus", "배송중", "orderDate", "2024-04-01"));
    }

    @Operation(summary = "주문 취소", description = "배송 전 상태인 상품의 주문을 취소합니다.")
    @PutMapping("/{orderId}/cancel")
    public Map<String, String> cancelOrder(@PathVariable Long orderId) {
        return Map.of("msg", "주문취소 성공!, 재고가 반환됩니다");
    }

    @Operation(summary = "반품 신청", description = "배송 완료된 상품을 반품 신청합니다.")
    @PutMapping("/{orderId}/return")
    public Map<String, String> returnOrder(@PathVariable Long orderId) {
        return Map.of("msg", "반품신청이 완료되었습니다. 회수는 하루가 소요됩니다");
    }
}
