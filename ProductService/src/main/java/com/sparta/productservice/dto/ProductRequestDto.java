package com.sparta.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(message = "상품명을 입력해주세요.")
    private String productName;

    @NotBlank(message = "상품 설명을 입력해주세요.")
    private String productDescription;

    @NotNull(message = "상품 가격을 입력해주세요.")
//    @Pattern(
//            regexp = "^(\\d{1,9})(\\.\\d{1,2})?$",
//            message = "올바른 상품 가격을 입력해주세요. 상품 가격은 최대 소수점 2자리까지만 입력할 수 있습니다."
//    )
    private long productPrice;

    @NotNull(message = "등록할 상품 수량을 입력해주세요.")
    private int stockQuantity;
}
