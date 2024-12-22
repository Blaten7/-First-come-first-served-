package com.sparta.userservice;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.sparta")
public class UserServiceApplication {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir")) // 프로젝트 루트 디렉토리 설정
                .load();

        System.setProperty("USER_SERVICE_DB_URL", dotenv.get("USER_SERVICE_DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PW", dotenv.get("DB_PW"));
    }

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
