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
    private Long orderNum; // 주문번호

    private String userEmail; // 사용자 아이디 식별자

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime orderDate; // 주문일자

    private String productName; // 상품 이름
    private String orderStatus; // 주문 상태
    private BigDecimal totalAmount; // 주문 수량

//    @OneToMany(mappedBy = "order")
//    private List<OrderItem> orderItems;
}
