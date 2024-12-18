package com.sparta.webapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(summary = "상품 리스트 조회", description = "등록된 상품 목록을 조회합니다.")
    @GetMapping
    public List<Map<String, Object>> getProducts() {
        return List.of(
                Map.of("productId", 1, "productName", "상품1", "productPrice", 10000, "stockQuantity", 20),
                Map.of("productId", 2, "productName", "상품2", "productPrice", 20000, "stockQuantity", 15)
        );
    }

    @Operation(summary = "상품 상세 정보 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public Map<String, Object> getProductDetails(@PathVariable Long productId) {
        return Map.of("productId", productId, "productName", "상품1",
                "productDescription", "상품 설명", "productPrice", 10000, "stockQuantity", 20);
    }
}
