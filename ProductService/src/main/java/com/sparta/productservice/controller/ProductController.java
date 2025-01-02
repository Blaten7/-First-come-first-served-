package com.sparta.productservice.controller;

import com.sparta.productservice.dto.ProductRequestDto;
import com.sparta.productservice.entity.Product;
import com.sparta.productservice.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;

    @Operation(summary = "상품 등록", description = "상품을 등록합니다.")
    @PutMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody ProductRequestDto productRequest) {
        log.info("상품 등록 컨트롤러 진입");
        Optional<Product> duplicationProduct = productRepository.findByProductName(productRequest.getProductName());
        if (duplicationProduct.isPresent()) {
            String responseMessage = """
                    이미 존재하는 상품입니다.
                    링크1: 상품 수량 업데이트 -> [POST] http://localhost:8222/api/product/updateQuantity
                    링크2: 상품 정보 전체 수정 -> [PUT] http://localhost:8222/api/product/updateDetails
                    """;
            return ResponseEntity.status(409).body(responseMessage);
        }

        Product product = new Product();
        product.setProductName(productRequest.getProductName());
        product.setProductDescription(productRequest.getProductDescription());
        product.setProductPrice(productRequest.getProductPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);
        return ResponseEntity.status(201).body("상품 등록이 완료되었습니다.");
    }
    @Operation(summary = "", description = "")
    @PostMapping("/updateQuantity")
    public ResponseEntity<String> updateQuantity(@Valid @RequestBody ProductRequestDto productRequest) {
        return null;
    }

    @Operation(summary = "", description = "")
    @PostMapping("/updateDetails")
    public ResponseEntity<String> updateDetails(@Valid @RequestBody ProductRequestDto productRequest) {
        return null;
    }

    @Operation(summary = "상품 리스트 조회", description = "등록된 상품 목록을 조회합니다.")
    @GetMapping("/view/list")
    public ResponseEntity<?> getProducts() {
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("notice", "조회할 상품 목록이 없습니다.");
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.status(200).body(products);
    }

    @Operation(summary = "상품 상세 정보 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/view")
    public ResponseEntity<?> getProductDetails(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long productId
    ) {
        if (productId != null) {
            return ResponseEntity.ok(productRepository.findById(productId));
        } else if (productName != null) {
            return ResponseEntity.ok(productRepository.findByProductName(productName));
        } else {
            return ResponseEntity.status(400).body("상품아이디 또는 상품이름중 하나는 입력해주세요 양심이 있으시면요");
        }
    }

    @Operation(summary = "주문서비스에서 요청보낼 메서드", description = "주문들어온 상품이 있기는 한건지")
    @PostMapping("/isExist")
    public boolean exist(@RequestParam String productName) {
        log.info("상품 존재 확인 여부 메서드 진입 \n상품이름 : {}", productName);
        return productRepository.existsByProductName(productName);
    }

    @Operation(summary = "주문서비스에서 요청보낼 메서드", description = "주문들어온 상품이 있기는 한건지")
    @PostMapping("/isOverQuantity")
    public boolean isOver(@RequestParam String productName, @RequestParam int orderQuantity) {
        log.info("상품 수량 확인 여부 메서드 진입");
        return productRepository.existsByProductNameAndStockQuantityGreaterThanEqual(productName, orderQuantity);
    }

    @Operation(summary = "주문!", description = "상품이름으로 검색해서 주문 수량만큼 재고를 감소")
    @PostMapping("/order")
    public void order(@RequestParam String productName, @RequestParam int orderQuantity) {
        log.info("주문한 상품 : {}// 주문 수량 : {}", productName, orderQuantity);
        productRepository.updateProductStockQuantityMinusOrderQuantity(productName, orderQuantity);
        log.info("주문완료");
    }

    @Operation(summary = "주문 취소", description = "상품이름으로 필터링, 취소 수량만큼 재고를 증가")
    @PostMapping("/cancel")
    public void cancel(@RequestParam String productName, @RequestParam int cancelQuantity) {
        log.info("취소한 상품 : {}// 취소 수량 : {}", productName, cancelQuantity);
        productRepository.updateProductStockQuantityPlusOrderQuantity(productName, cancelQuantity);
        log.info("주문 취소완료");
    }

    @Operation(summary = "선착순 구매 상품 실시간 조회", description = "제곧내")
    @PostMapping("/live/stock")
    public int liveStock() {
        String productName = "선착순";
        return productRepository.findByProductName2(productName);
    }
}
