package com.sparta.orderservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("./OrderService/src/main/resources") // .env 파일 위치 설정 (루트 디렉토리)
                .load();

        // .env 값을 시스템 속성으로 등록
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}

