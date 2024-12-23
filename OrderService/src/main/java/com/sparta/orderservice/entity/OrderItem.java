package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "`orderItem`")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name = "orderNum")
    private Order order;

//    @ManyToOne
//    @JoinColumn(name = "productId")
//    private Product product;
}
