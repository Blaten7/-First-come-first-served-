package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "wishlist")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    private String userEmail;

    private String productName;
//    같은 스키마면 원래 이게 맞음.
//    @ManyToOne
//    @JoinColumn(name = "userId")
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "productId")
//    private Product product;

    private Integer quantity;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
}
