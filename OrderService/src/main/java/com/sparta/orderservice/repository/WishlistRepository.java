package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserEmail(String email);

    @Transactional
    @Modifying
    @Query("update Wishlist " +
            "set quantity = :quantity " +
            "where wishlistId = :wishlistId")
    void findByWishlistIdAndUpdateQuantity(Long wishlistId, int quantity);
}
