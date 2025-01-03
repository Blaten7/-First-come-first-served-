package com.sparta.orderservice.controller;

import com.sparta.orderservice.connector.ProductServiceConnector;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.Product;
import com.sparta.orderservice.entity.Order;
import com.sparta.orderservice.entity.Wishlist;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.WishlistRepository;
import com.sparta.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

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
    public ResponseEntity<String> test(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>("아아 여기는 OrderService 요청 확인!", HttpStatus.OK);
    }

    @Operation(summary = "주문 또는 찜하기", description = "상품을 주문 또는 위시리스트에 추가합니다.")
    @PostMapping("/product/{where}")
    public Mono<ResponseEntity<String>> order(
            @PathVariable("where") String where,
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequestDto orderRequest) {
        String productName = orderRequest.getProductName();
        int orderQuantity = orderRequest.getStockQuantity();

        Flux<Product> productOpt = productServiceConnector.findByProductNameAndOverQuantity(productName);

        return productOpt.next() // Flux에서 첫 번째 값을 Mono로 가져오기
                .flatMap(product -> {
                    if (product.getStockQuantity() < orderQuantity) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("상품 재고가 주문 수량보다 적습니다."));
                    }
                    String email = orderService.extractEmail(token.replace("Bearer ", ""));
                    switch (where) {
                        case "주문":
                            // 주문 생성
                            Order order = new Order();
                            order.setUserEmail(email);
                            order.setProductName(productName);
                            order.setOrderDate(LocalDateTime.now());
                            order.setOrderStatus("배송 준비중");
                            order.setTotalAmount(orderQuantity);
                            orderRepository.save(order); // 동기 방식으로 저장
                            // 재고 감소
                            productServiceConnector.orderProduct(productName, orderQuantity);
                            return Mono.just(ResponseEntity.ok("상품 주문이 완료되었습니다."));
                        case "찜":
                            Wishlist wishlist = new Wishlist();
                            wishlist.setCreatedAt(LocalDateTime.now());
                            wishlist.setQuantity(orderQuantity);
                            wishlist.setProductName(productName);
                            wishlist.setUserEmail(email);
                            wishlistRepository.save(wishlist);
                            return Mono.just(ResponseEntity.ok("위시리스트에 추가되었습니다."));
                    }
                    return Mono.just(ResponseEntity.ok("주문 로직 정상 실행 완료"));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("존재하지 않는 상품입니다.")));
    }

    @Operation(summary = "주문 상태 조회", description = "사용자의 주문 상태를 조회합니다.")
    @GetMapping("/view/order/status")
    public ResponseEntity<?> getOrderStatus(@RequestHeader("Authorization") String token) {

        token = token.replace("Bearer ", "");
        String email = orderService.extractEmail(token);
        List<Order> orderList = orderRepository.findByUserEmail(email);

        if (orderList.isEmpty()) return ResponseEntity.status(403).body("주문한 상품이 존재하지 않습니다");

        return ResponseEntity.status(200).body(orderList);
    }

    @Operation(summary = "주문 취소", description = "배송 전 상태인 상품의 주문을 취소합니다.")
    @Transactional
    @PatchMapping("/cancel/{productName}")
    public ResponseEntity<String> cancelOrder(@RequestHeader("Authorization") String token,
                                              @PathVariable String productName) {

        String email = orderService.extractEmail(token);
        String preDelivery = "배송 준비중";
        int cancelQuantity = orderRepository.findTotalAmountByUserEmailAndOrderStatusAndProductName(email, preDelivery, productName);
        orderRepository.updateByUserEmailAndOrderStatus(email, preDelivery);
        productServiceConnector.cancelProduct(productName, cancelQuantity);
        return ResponseEntity.status(200).body("주문이 취소되었습니다.");
    }

    @Operation(summary = "반품 신청", description = "배송 완료된 상품을 반품 신청합니다.")
    @PatchMapping("/refund/{productName}")
    public ResponseEntity<String> returnOrder(@RequestHeader("Authorization") String token,
                                              @PathVariable String productName) {
        String email = orderService.extractEmail(token);
        String orderStatus = "배송 완료";
        Optional<Order> orderList = orderRepository.findByUserEmailAndProductNameAndOrderStatus(email, productName, orderStatus);
        if (orderList.isEmpty()) return ResponseEntity.status(404).body("배송 완료된 상품이 아닙니다.");

        orderRepository.updateOrderStatusByUserEmailAndProductName(email, productName);
        return ResponseEntity.status(200).body("반품 신청이 완료되었습니다");
    }

    @Operation(summary = "위시리스트 상품 주문", description = "위시리스트에서 선택한 상품을 주문합니다.")
    @PostMapping("/wishlist/order/{wishlistId}")
    public Mono<ResponseEntity<String>> orderFromWishlist(
            @RequestHeader("Authorization") String token,
            @PathVariable Long wishlistId) {

        // 위시리스트 확인
        Optional<Wishlist> wishlistOptional = wishlistRepository.findById(wishlistId);
        if (wishlistOptional.isEmpty()) {
            return Mono.just(ResponseEntity.status(404).body("위시리스트에 해당 상품이 존재하지 않습니다."));
        }

        Wishlist wishlist = wishlistOptional.get();
        String productName = wishlist.getProductName();
        int orderQuantity = wishlist.getQuantity();

        // 상품 검증
        Flux<Product> productOpt = productServiceConnector.findByProductNameAndOverQuantity(productName);

        return productOpt.next() // Flux에서 첫 번째 값을 Mono로 가져오기
                .flatMap(product -> {
                    // 재고 확인
                    log.info("상품 재고 : " + product.getStockQuantity());
                    if (product.getStockQuantity() < orderQuantity) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("상품 재고가 주문 수량보다 적습니다."));
                    }

                    // 주문 생성
                    String email = orderService.extractEmail(token.replace("Bearer ", ""));
                    Order order = new Order();
                    order.setUserEmail(email);
                    order.setProductName(productName);
                    order.setOrderDate(LocalDateTime.now());
                    order.setOrderStatus("배송 준비중");
                    order.setTotalAmount(orderQuantity);

                    orderRepository.save(order); // 동기 방식으로 저장

                    // 재고 감소
                    productServiceConnector.orderProduct(productName, orderQuantity);

                    // 위시리스트에서 삭제
                    wishlistRepository.delete(wishlist);

                    return Mono.just(ResponseEntity.ok("위시리스트 상품이 주문되었습니다."));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("존재하지 않는 상품입니다.")));
    }


    @Operation(summary = "마이페이지 조회", description = "위시리스트와 주문 상태를 함께 조회합니다.")
    @GetMapping("/mypage")
    public ResponseEntity<Map<String, Object>> getMyPage(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = orderService.extractEmail(token);

        List<Wishlist> wishlist = wishlistRepository.findByUserEmail(email);
        List<Order> orders = orderRepository.findByUserEmail(email);

        return ResponseEntity.status(200).body(Map.of(
                "wishlist", wishlist,
                "orders", orders
        ));
    }


    @Operation(summary = "위시리스트 조회", description = "위시리스트에 등록된 상품을 조회합니다.")
    @GetMapping("/wishlist")
    public ResponseEntity<?> getWishlist(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = orderService.extractEmail(token);
        List<Wishlist> wishlist = wishlistRepository.findByUserEmail(email);

        if (wishlist.isEmpty()) {
            return ResponseEntity.status(404).body("위시리스트가 존재하지 않습니다.");
        }

        return ResponseEntity.ok(wishlist);
    }


    @Operation(summary = "위시리스트 수정", description = "위시리스트 상품 수량을 수정합니다.")
    @PatchMapping("/wishlist/quantity/update/{wishlistId}")
    public ResponseEntity<String> updateWishlist(
            @PathVariable Long wishlistId,
            @RequestParam int quantity,
            @RequestHeader("Authorization") String token) {

        Optional<Wishlist> optionalWishlist = wishlistRepository.findById(wishlistId);
        if (optionalWishlist.isEmpty()) {
            return ResponseEntity.status(404).body("위시리스트 항목을 찾을 수 없습니다.");
        }
        wishlistRepository.findByWishlistIdAndUpdateQuantity(wishlistId, quantity);

        return ResponseEntity.ok("위시리스트 수정이 완료되었습니다.");
    }

    @Operation(summary = "위시리스트 삭제", description = "위시리스트에서 상품을 삭제합니다.")
    @DeleteMapping("/wishlist/delete/{wishlistId}")
    public ResponseEntity<String> deleteFromWishlist(
            @PathVariable Long wishlistId,
            @RequestHeader("Authorization") String token) {
        if (!wishlistRepository.existsById(wishlistId)) {
            return ResponseEntity.status(404).body("위시리스트 항목을 찾을 수 없습니다.");
        }
        wishlistRepository.deleteById(wishlistId);
        return ResponseEntity.ok("위시리스트 항목이 삭제되었습니다.");
    }

    @Operation(summary = "결제 프로세스 시작", description = "선착순 구매 상품 한정")
    @PostMapping("/purchase/start")
    public void purchaseStart(@RequestParam String token) {
        log.info("[주문] 결제 프로세스 시작");
        token = token.replace("Bearer ", "");
        String email = orderService.extractEmail(token);
        orderRepository.updateOrderStatusPurchaseStart(email);
        String productName = "선착순";
        productServiceConnector.orderProduct(productName, 1);
    }

    @Operation(summary = "결제 프로세스 완료", description = "선착순 구매 상품 한정")
    @PostMapping("/purchase/end")
    public void purchaseComplete(@RequestParam  String token) {
        log.info("[주문] 결제 프로세스 완료");
        token = token.replace("Bearer ", "");
        String email = orderService.extractEmail(token);
        orderRepository.updateOrderStatusPurchaseComplete(email);
    }
}