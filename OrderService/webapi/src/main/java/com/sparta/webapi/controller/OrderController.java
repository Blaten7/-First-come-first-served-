package com.sparta.webapi.controller;

import com.sparta.domain.dto.OrderRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    /*
        요구사항.
        =사용자는 노출된 상품에 한해 주문 및 위시리스트에 등록 가능. -> 로그인 한 유저만 사용가능
            1주차에서는 재고 부족으로 인한 주문 불가는 없다고 가정한다고 되어 있는데. 이건 뭔가 하고 싶네. 나 청개구린가?
        마이페이지를 통해 위시리스트에 등록한 상품과 주문한 상품의 상태를 조회 할 수 있습니다.
            * 위시리스트에서는 내가 등록한 상품에 대한 정보를 보여주는 기능을 기본으로 하고
              =제품의 상세페이지로 이동
              =상품의 수량 변경 및 주문
              =위시리스트 내 항목의 수정
            * 주문 내역에서는 사용자가 주문한 상품에 대한 상태를 보여주고
              상품에 대한 주문 취소, 반품 기능을 제공
              =주문 상품에 대한 상태조회 [ 주문 후 D+1일에 배송중. D+2일에 배송 완료로 자동 상태변경 ]
              =주문 상품에 대한 취소
                주문 상태가 배송 중이 되기 이전까지만 취소가 가능.
                취소 후에는 상품의 재고가 복구 되어야 하며, 취소후 상태는 취소완료로 변경
              =상품에 대한 반품
                배송 완료 후 D+1일까지만 반품 가능. 그 이후엔 안됨
                배송 완료가 된 상품에 대해서만 반품이 가능.
                반품한 상품은 반품신청후 D+1일에 재고에 반영.
                재고 반영 후 상태는 반품완료로 변경
     */

//    @Operation(summary = "주문하기", description = "상품을 주문합니다.")
//    @PostMapping
//    public ResponseEntity<?> placeOrder(
//            @RequestHeader("Authorization") String token,
//            @RequestBody OrderRequestDto orderRequest) {
//        // 1. 인증 토큰 검증
//        if (!isValidToken(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body("로그인이 필요합니다.");
//        }
//
//        // 2. 상품 노출 여부 확인
//        if (!isProductVisible(orderRequest.getProductId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("노출되지 않은 상품은 주문할 수 없습니다.");
//        }
//
//        // 3. 주문 처리 로직
//        OrderResponse orderResponse = processOrder(orderRequest);
//
//        // 4. 성공 응답
//        return ResponseEntity.ok(orderResponse);
//    }



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
