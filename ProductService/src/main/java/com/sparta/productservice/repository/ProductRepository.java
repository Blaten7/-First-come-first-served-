package com.sparta.productservice.repository;

import com.sparta.productservice.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductName(String productName);

    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    List<Product> findByProductName2(String productName);

//    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
//            "FROM Product p WHERE p.productName = :productName AND p.stockQuantity >= :orderQuantity")
//    List<Product> findByProductNameAndStockQuantityGreaterThanEqual(@Param("productName") String productName, @Param("orderQuantity") int orderQuantity);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :orderQuantity WHERE p.productName = :productName AND p.stockQuantity >= :orderQuantity")
    void updateProductStockQuantityMinusOrderQuantity(@Param("productName") String productName, @Param("orderQuantity") int orderQuantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :cancelQuantity WHERE p.productName = :productName")
    void updateProductStockQuantityPlusOrderQuantity(@Param("productName") String productName, @Param("cancelQuantity") int cancelQuantity);

    boolean existsByProductName(String productName);
}
