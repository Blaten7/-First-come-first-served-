package com.sparta.userservice.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class DatabaseConfig {

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
//                .option(DRIVER, "mysql")
//                .option(HOST, "db.cfu4cgmokcze.ap-northeast-2.rds.amazonaws.com")
//                .option(PORT, 3306)
//                .option(USER, "admin")
//                .option(PASSWORD, "dlgudrn1998")
//                .option(DATABASE, "MemberService")
//                .option(SSL, false) // SSL 활성화
//                .build());
//    }
//@Bean
//public ConnectionFactory connectionFactory() {
//    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder()
//            .url("r2dbc:mysql://db.cfu4cgmokcze.ap-northeast-2.rds.amazonaws.com:3306/MemberService")
//            .username("admin")
//            .password("dlgudrn1998")
//            .initialSize(5) // 초기 연결 수
//            .maxSize(20) // 최대 연결 수
//            .build();
//    return new ConnectionPool(configuration);
//}
    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder.withUrl("r2dbc:mysql://db.cfu4cgmokcze.ap-northeast-2.rds.amazonaws.com:3306/MemberService")
                .username("admin")
                .password("dlgudrn1998")
                .build();
    }

}
