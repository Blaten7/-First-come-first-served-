package com.sparta.orderservice.controller;

import com.sparta.orderservice.connector.ProductServiceConnector;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.connector.UserServiceConnector;
import com.sparta.orderservice.entity.Order;
import com.sparta.orderservice.entity.Wishlist;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.WishlistRepository;
import com.sparta.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final UserServiceConnector userServiceConnector;
    private final ProductServiceConnector productServiceConnector;

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;
    /*
        요구사항.
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
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("아아 여기는 OrderService 요청 확인!", HttpStatus.OK);
    }

    @Operation(summary = "주문 또는 찜하기", description = "상품을 주문 또는 위시리스트에 추가합니다.")
    @PostMapping("/product/{where}")
    public ResponseEntity<String> order(
            @PathVariable("where") String where,
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequestDto orderRequest) {
        // 로그인 여부 확인
        if (!userServiceConnector.isValidToken(token)) return ResponseEntity.status(403).body("로그인이 필요한 서비스 입니다.");
        String productName = orderRequest.getProductName();
        Integer orderQuantity = orderRequest.getStockQuantity();

        // 상품 존재하는지, 그리고 재고가 주문수량 이상 있는지 검증
        if (!productServiceConnector.isProductExistAndQuantityIsOverOrderQuantity(productName, orderQuantity)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("존재하지 않는 상품입니다.");

        // 상품이 존재한다면 주문자의 아이디를 토큰에서 추출
        String email = orderService.extractEmail(token);
        if (where.equals("주문")) {
            // 주문 수량만큼 상품 재고량 감소
            productServiceConnector.orderProduct(productName, orderQuantity);
            // 주문 내역을 주문 엔티티에 저장
            Order order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setOrderStatus("배송 준비중");
            order.setTotalAmount(BigDecimal.valueOf(orderQuantity));
            orderRepository.save(order);
            return ResponseEntity.status(200).body("주문이 정상 처리되었습니다.");
        } else if (where.equals("찜")) {
            Wishlist wishlist = new Wishlist();
            wishlist.setCreatedAt(LocalDateTime.now());
            wishlist.setQuantity(orderQuantity);
            wishlist.setProductName(productName);
            wishlist.setUserEmail(email);
            wishlistRepository.save(wishlist);
            return ResponseEntity.status(200).body("위시리스트에 정상 추가되었습니다.");
        }
        return ResponseEntity.status(404).body("주문 또는 찜 선택은 필수 사항입니다,");
    }
    /*
        사용자의 아이디 즉 이메일에 한한 주문상태 조회가 필요함.
        현재 Order 엔티티의 유저이메일 연관관계 매핑문제로 비활성화 상태임.
        로직 변경 및 구성 요망
    */
    @Operation(summary = "주문 상태 조회", description = "사용자의 주문 상태를 조회합니다.")
    @GetMapping("/view/order/status")
    public ResponseEntity<?> getOrderStatus(@RequestHeader("Authorization") String token) {
        String email = orderService.extractEmail(token);
        List<Order> orderList = orderRepository.findAll();

        if (orderList.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("notice", "주문한 상품이 존재하지 않습니다");
            return ResponseEntity.status(403).body(response);
        }
        return ResponseEntity.status(200).body(orderList);
    }
    /*
        사용자의 로그인 토큰과 취소할 상품의 이름과 // 수량을 받고, 수량은 제외. 그냥 전부 취소시키는걸ㄹ ㅗ핪디ㅏ
        사용자의 주문목록에 상품의 이름이 있을까 없을까
        있다면 주문 상태가 배송 중 혹은 배송 완료가 아닌지 확인
        아니면 주문취소후 상품서비스 가서 해당 수량만큼 재고 다시 증가
        그리고 주문목록에서도 삭제.
     */
    @Operation(summary = "주문 취소", description = "배송 전 상태인 상품의 주문을 취소합니다.")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@RequestHeader("Authorization") String token) {
        String email = orderService.extractEmail(token);
//        orderRepository.findByUserEmailAndUpdateOrderStatus();

        return ResponseEntity.status(200).body("주문이 취소되었습니다.");
    }
    /*
        토큰과 상품이름을 받고
        사용자가 로그인 했는지 확인
        했다면 토큰에서 이메일 추출
        이메일 값으로 주문목록에서 상품이름이면서 배송완료가 있는지 확인
        있다면 반품신청으로 상태변경.
        D+1일후에 회수완료로 상태변경하고 상품서비스로가서 재고 수량만큼 추가
     */
    @Operation(summary = "반품 신청", description = "배송 완료된 상품을 반품 신청합니다.")
    @PutMapping("/{orderId}/return")
    public Map<String, String> returnOrder(@RequestHeader("Authorization") String token) {
        return Map.of("msg", "반품신청이 완료되었습니다. 회수는 하루가 소요됩니다");
    }
}