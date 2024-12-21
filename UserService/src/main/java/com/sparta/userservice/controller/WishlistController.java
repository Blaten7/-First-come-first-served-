package com.sparta.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Operation(summary = "위시리스트에 상품 추가", description = "상품을 위시리스트에 추가합니다.")
    @PostMapping
    public Map<String, String> addToWishlist(@RequestBody Map<String, Object> wishlistRequest) {
        return Map.of("msg", "해당 상품을 '찜' 하였습니다");
    }

    @Operation(summary = "위시리스트 조회", description = "위시리스트에 등록된 상품을 조회합니다.")
    @GetMapping
    public List<Map<String, Object>> getWishlist() {
        return List.of(Map.of("wishlistId", 1, "productId", 1, "productName", "상품1", "quantity", 2));
    }

    @Operation(summary = "위시리스트 수정", description = "위시리스트 상품 수량을 수정합니다.")
    @PutMapping("/{wishlistId}")
    public Map<String, String> updateWishlist(@PathVariable Long wishlistId, @RequestBody Map<String, Integer> updateRequest) {
        return Map.of("msg", "위시리스트 수정이 완료되었습니다");
    }

    @Operation(summary = "위시리스트 삭제", description = "위시리스트에서 상품을 삭제합니다.")
    @DeleteMapping("/{wishlistId}")
    public Map<String, String> deleteFromWishlist(@PathVariable Long wishlistId) {
        return Map.of("msg", "상품이 위시리스트에서 삭제되었습니다");
    }
}
