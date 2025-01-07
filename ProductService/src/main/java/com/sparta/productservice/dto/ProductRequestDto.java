package com.sparta.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDto {

    @NotBlank(message = "상품명을 입력해주세요.")
    private String productName;

    private String productDescription;

    @NotNull(message = "상품 가격을 입력해주세요.")
    private long productPrice;

    @NotNull(message = "등록할 상품 수량을 입력해주세요.")
    private int stockQuantity;

    public ProductRequestDto() {
    }
}
