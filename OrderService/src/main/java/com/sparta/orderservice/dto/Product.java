package com.sparta.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Product {
    private String productName;
    private Integer stockQuantity;

    @JsonCreator
    public Product(
            @JsonProperty("productName") String productName,
            @JsonProperty("stockQuantity") Integer stockQuantity) {
        this.productName = productName;
        this.stockQuantity = stockQuantity;
    }

    public Product() {

    }
}
