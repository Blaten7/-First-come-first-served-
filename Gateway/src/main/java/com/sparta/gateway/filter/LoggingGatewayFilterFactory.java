package com.sparta.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGatewayFilterFactory.class);

    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestUrl = exchange.getRequest().getURI().toString();
            logger.info("Incoming request URL: {}", requestUrl);

            // Response 상태코드 로그 추가
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();
                logger.info("Response status code: {}", statusCode);
            }));
        };
    }

    public static class Config {
        // 필터 설정이 필요할 경우 여기에 추가
    }
}
