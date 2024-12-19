package com.sparta.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.sparta.domain.entity")
@EnableJpaRepositories(basePackages = "com.sparta.domain.repository")
@SpringBootApplication(scanBasePackages = "com.sparta")
public class WebapiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebapiApplication.class, args);
    }

}
