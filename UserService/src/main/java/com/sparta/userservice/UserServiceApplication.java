package com.sparta.userservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.class
})
public class UserServiceApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("./UserService/src/main/resources") // .env 파일 위치 설정 (루트 디렉토리)
                .load();

        // .env 값을 시스템 속성으로 등록
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
