package com.sparta.purchaseservice.dto;

import lombok.Data;

@Data
public class Product {
    private int stock; // 재고 수량

    public Product() {
        // Default constructor
    }

    public Product(int stock) {
        this.stock = stock;
    }

}
