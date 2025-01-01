package com.sparta.purchaseservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PurchaseServiceApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("./PurchaseService/src/main/resources") // .env 파일 위치 설정 (루트 디렉토리)
                .load();

        // .env 값을 시스템 속성으로 등록
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(PurchaseServiceApplication.class, args);
    }

}
