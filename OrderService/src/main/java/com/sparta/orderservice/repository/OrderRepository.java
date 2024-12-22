package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmail(String email);

    void deleteByUserEmailAndOrderStatus(String email, String preDelivery);

//    @Query("SELECT SUM(o.totalAmount) " +
//            "FROM Order o " +
//            "JOIN o.orderItems oi " +
//            "WHERE o.user.email = :email " +
//            "AND o.orderStatus = :status " +
//            "AND oi.productName = :productName")
//    Long findTotalQuantityByUserAndStatusAndProductName(
//            @Param("email") String email,
//            @Param("status") String status,
//            @Param("productName") String productName);

    int findTotalAmountByUserEmailAndOrderStatusAndProductName(String email, String preDelivery, String productName);

    Optional<Order> findByUserEmailAndProductNameAndOrderStatus(String email, String productName, String orderStatus);

    @Modifying
    @Transactional
    @Query("update Order " +
            "set orderStatus = '반품 신청' " +
            "where userEmail = :email and " +
            "productName = :productName")
    void updateOrderStatusByUserEmailAndProductName(String email, String productName);
}
