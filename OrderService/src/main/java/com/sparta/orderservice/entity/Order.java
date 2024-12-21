package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderNum;

//    @ManyToOne
//    @JoinColumn(name = "userId", referencedColumnName = "id")
//    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime orderDate;

    private String orderStatus;
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
}
