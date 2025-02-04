package com.sparta.purchaseservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Product {
    private Long productId;
    private String productName;
    private String productDescription;
    private long productPrice;
    private Integer stockQuantity;

    @JsonCreator
    public Product(
            @JsonProperty("productId") Long productId,
            @JsonProperty("productName") String productName,
            @JsonProperty("productDescription") String productDescription,
            @JsonProperty("productPrice") long productPrice,
            @JsonProperty("stockQuantity") Integer stockQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.stockQuantity = stockQuantity;
    }
}
