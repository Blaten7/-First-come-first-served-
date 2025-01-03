package com.sparta.productservice.controller;

import com.sparta.productservice.dto.ProductRequestDto;
import com.sparta.productservice.entity.Product;
import com.sparta.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void testRegisterProduct_success() {
        // Given
        ProductRequestDto requestDto = new ProductRequestDto("Test Product", "Description",1000, 10);
        when(productRepository.findByProductName(requestDto.getProductName())).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = productController.register(requestDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo("상품 등록이 완료되었습니다.");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testRegisterProduct_duplicate() {
        // Given
        ProductRequestDto requestDto = new ProductRequestDto("Test Product", "Description", 1000, 10);
        when(productRepository.findByProductName(requestDto.getProductName())).thenReturn(Optional.of(new Product()));

        // When
        ResponseEntity<String> response = productController.register(requestDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody()).contains("이미 존재하는 상품입니다.");
    }

    @Test
    void testGetProducts_noProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(List.of());

        // When
        ResponseEntity<?> response = productController.getProducts();

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    void testGetProducts_withProducts() {
        // Given
        Product product = new Product();
        product.setProductName("Test Product");
        product.setStockQuantity(10);
        when(productRepository.findAll()).thenReturn(List.of(product));

        // When
        ResponseEntity<?> response = productController.getProducts();

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    void testGetProductDetails_byId() {
        // Given
        Product product = new Product();
        product.setProductName("Test Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        ResponseEntity<?> response = productController.getProductDetails(null);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(Optional.of(product));
    }

//    @Test
//    void testGetProductDetails_byName() {
//        // Given
//        Product product = new Product();
//        product.setProductName("Test Product");
//        when(productRepository.findByProductName("Test Product")).thenReturn(Optional.of(product));
//
//        // When
//        ResponseEntity<?> response = productController.getProductDetails("Test Product", null);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody()).isEqualTo(Optional.of(product));
//    }

    @Test
    void testExistProduct() {
        // Given
        when(productRepository.existsByProductName("Test Product")).thenReturn(true);

        // When
        boolean result = productController.exist("Test Product");

        // Then
        assertThat(result).isTrue();
    }

//    @Test
//    void testIsOverQuantity() {
//        // Given
//        when(productRepository.existsByProductNameAndStockQuantityGreaterThanEqual("Test Product", 5)).thenReturn(true);
//
//        // When
//        boolean result = productController.isOver("Test Product", 5);
//
//        // Then
//        assertThat(result).isTrue();
//    }

    @Test
    void testOrderProduct() {
        // When
        productController.order("Test Product", 5);

        // Then
        verify(productRepository, times(1)).updateProductStockQuantityMinusOrderQuantity("Test Product", 5);
    }

    @Test
    void testCancelProduct() {
        // When
        productController.cancel("Test Product", 5);

        // Then
        verify(productRepository, times(1)).updateProductStockQuantityPlusOrderQuantity("Test Product", 5);
    }
}
