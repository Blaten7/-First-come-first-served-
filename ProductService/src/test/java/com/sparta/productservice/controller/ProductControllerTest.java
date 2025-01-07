package com.sparta.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.productservice.dto.ProductRequestDto;
import com.sparta.productservice.entity.Product;
import com.sparta.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductRepository productRepository;

    private ProductController productController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        productController = new ProductController(productRepository);
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .build();
    }

    @Nested
    class 등록 {
        @Test
        @DisplayName("상품 등록 성공")
        void registerProductSuccess() throws Exception {
            // given
            ProductRequestDto request = new ProductRequestDto();
            request.setProductName("테스트상품");
            request.setProductDescription("테스트설명");
            request.setProductPrice(10000);
            request.setStockQuantity(100);

            // when & then
            mockMvc.perform(put("/api/product/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("상품 중복 등록 실패")
        void registerProductDuplicate() throws Exception {
            // given
            ProductRequestDto request = new ProductRequestDto();
            request.setProductName("테스트상품");
            request.setProductDescription("테스트설명");
            request.setProductPrice(10000);
            request.setStockQuantity(100);

            // Mock 동작 수정
            Product existingProduct = new Product();
            existingProduct.setProductName("테스트상품");
            when(productRepository.findByProductName("테스트상품"))
                    .thenReturn(Optional.of(existingProduct));

            // when & then
            mockMvc.perform(put("/api/product/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }


    @Nested
    class 업데이트 {
        @Test
        @DisplayName("상품 정보 전체 수정 성공")
        void updateDetailsSuccess() throws Exception {
            // given
            ProductRequestDto request = new ProductRequestDto();
            request.setProductName("테스트상품");
            request.setProductDescription("수정된설명");
            request.setProductPrice(20000);

            when(productRepository.findByProductName(anyString()))
                    .thenReturn(Optional.of(new Product()));

            // when & then
            mockMvc.perform(post("/api/product/updateDetails")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("상품 수량 업데이트 성공")
        void updateQuantitySuccess() throws Exception {
            // given
            String productName = "테스트상품";
            int newQuantity = 100;

            ProductRequestDto request = new ProductRequestDto();
            request.setProductName(productName);
            request.setStockQuantity(newQuantity);

            Product product = new Product();
            product.setProductName(productName);

            when(productRepository.findByProductName(productName))
                    .thenReturn(Optional.of(product));

            // when & then
            mockMvc.perform(post("/api/product/updateQuantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(String.valueOf(newQuantity))));
        }

        @Test
        @DisplayName("상품 수량 업데이트 실패 - 상품 없음")
        void updateQuantityProductNotFound() throws Exception {
            // given
            ProductRequestDto request = new ProductRequestDto();
            request.setProductName("존재하지않는상품");
            request.setStockQuantity(100);

            when(productRepository.findByProductName(anyString()))
                    .thenReturn(Optional.empty());

            // when & then
            mockMvc.perform(post("/api/product/updateQuantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 조회 {
        @Test
        @DisplayName("상품 목록 조회 성공")
        void getProductsSuccess() throws Exception {
            // given
            List<Product> products = Arrays.asList(new Product(), new Product());
            when(productRepository.findAll()).thenReturn(products);

            // when & then
            mockMvc.perform(get("/api/product/view/list"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("상품 목록 조회 - 데이터 없음")
        void getProductsEmpty() throws Exception {
            // given
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/api/product/view/list"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("상품 상세 정보 조회 성공")
        void getProductDetailsSuccess() throws Exception {
            // given
            String productName = "테스트상품";
            when(productRepository.findByProductName(productName))
                    .thenReturn(Optional.of(new Product()));

            // when & then
            mockMvc.perform(get("/api/product/view")
                            .param("productName", productName))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("상품 상세 정보 조회 실패 - 파라미터 없음")
        void getProductDetailsFailNoParam() throws Exception {
            String productName = "";

            // when & then
            mockMvc.perform(get("/api/product/view")
                            .param("productName", productName))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실시간 재고 조회 성공")
        void liveStockSuccess() throws Exception {
            // given
            List<Product> products = Arrays.asList(new Product(), new Product());
            when(productRepository.findByProductName2("선착순")).thenReturn(products);

            // when & then
            mockMvc.perform(post("/api/product/live/stock"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("상품 존재 여부 확인 - 존재함")
    void existProductTrue() throws Exception {
        // given
        String productName = "테스트상품";
        when(productRepository.existsByProductName(productName)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/product/isExist")
                        .param("productName", productName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("상품 존재 여부 확인 - 존재하지 않음")
    void existProductFalse() throws Exception {
        // given
        String productName = "존재하지않는상품";
        when(productRepository.existsByProductName(productName)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/product/isExist")
                        .param("productName", productName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("재고 확인 성공")
    void isOverSuccess() throws Exception {
        // given
        String productName = "테스트상품";
        Product product = new Product();
        product.setStockQuantity(100);
        when(productRepository.findByProductName(productName))
                .thenReturn(Optional.of(product));

        // when & then
        mockMvc.perform(post("/api/product/isOverQuantity")
                        .param("productName", productName))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 처리 성공")
    void orderSuccess() throws Exception {
        // given
        String productName = "테스트상품";
        int orderQuantity = 10;

        // when & then
        mockMvc.perform(post("/api/product/order")
                        .param("productName", productName)
                        .param("orderQuantity", String.valueOf(orderQuantity)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(productRepository).updateProductStockQuantityMinusOrderQuantity(productName, orderQuantity);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelSuccess() throws Exception {
        // given
        String productName = "테스트상품";
        int cancelQuantity = 5;

        // when & then
        mockMvc.perform(post("/api/product/cancel")
                        .param("productName", productName)
                        .param("cancelQuantity", String.valueOf(cancelQuantity)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(productRepository).updateProductStockQuantityPlusOrderQuantity(productName, cancelQuantity);
    }

}