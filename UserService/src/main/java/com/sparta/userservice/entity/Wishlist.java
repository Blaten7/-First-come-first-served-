package com.sparta.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "wishlist")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

//    @ManyToOne
//    @JoinColumn(name = "productId")
//    private Product product;

    private Integer quantity;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
