package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmail(String email);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userEmail = :email AND o.orderStatus = :orderStatus AND o.productName = :productName")
    int findTotalAmountByUserEmailAndOrderStatusAndProductName(
            @Param("email") String email,
            @Param("orderStatus") String orderStatus,
            @Param("productName") String productName
    );


    Optional<Order> findByUserEmailAndProductNameAndOrderStatus(String email, String productName, String orderStatus);

    @Modifying
    @Transactional
    @Query("update Order " +
            "set orderStatus = '반품 신청' " +
            "where userEmail = :email and " +
            "productName = :productName")
    void updateOrderStatusByUserEmailAndProductName(String email, String productName);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = '배송중' " +
            "WHERE o.orderStatus = '배송 준비중' AND o.orderDate <= :thresholdTime")
    void updateOrderStatusIfReady(LocalDateTime thresholdTime);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = '배송 완료' " +
            "WHERE o.orderStatus = '배송중' AND o.orderDate <= :thresholdTime")
    void updateOrderStatusToCompleted(@Param("thresholdTime") LocalDateTime thresholdTime);

    @Transactional
    @Modifying
    @Query("update Order o " +
            "set o.orderStatus = '주문 취소' " +
            "where o.userEmail = :email and " +
            "o.orderStatus = :preDelivery")
    void updateByUserEmailAndOrderStatus(String email, String preDelivery);

    @Transactional
    @Modifying
    @Query("update Order o " +
            "set o.orderStatus = '결제 시도중' " +
            "where o.userEmail = :email")
    void updateOrderStatusPurchaseStart(String email);

    @Transactional
    @Modifying
    @Query("update Order o " +
            "set o.orderStatus = '결제 및 주문 완료' " +
            "where o.userEmail = :email")
    void updateOrderStatusPurchaseComplete(String email);
}
