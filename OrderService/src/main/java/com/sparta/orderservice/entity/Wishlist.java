package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "`wishlist`")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    private String userEmail;

    private String productName;

    private Integer quantity;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
}
