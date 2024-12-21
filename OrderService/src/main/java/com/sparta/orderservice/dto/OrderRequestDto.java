package com.sparta.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDto {

    @NotBlank(message = "주문할 상품 이름을 입력해주세요.")
    private String productName;

    @NotNull(message = "주문할 상품 수량을 입력해주세요.")
    private Integer stockQuantity;
}
