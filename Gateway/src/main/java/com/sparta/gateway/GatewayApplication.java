package com.sparta.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("./Gateway/src/main/resources") // .env 파일 위치 설정 (루트 디렉토리)
                .load();

        // .env 값을 시스템 속성으로 등록
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
        System.out.println("SECRET_KEY: " + dotenv.get("SECRET_KEY"));
        SpringApplication.run(GatewayApplication.class, args);
    }

}
