package com.sparta.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableFeignClients
@EntityScan(basePackages = "com.sparta.domain.entity")
@EnableJpaRepositories(basePackages = "com.sparta.domain.repository")
@SpringBootApplication(scanBasePackages = "com.sparta")
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
