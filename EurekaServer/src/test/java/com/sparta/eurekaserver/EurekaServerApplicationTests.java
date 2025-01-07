package com.sparta.eurekaserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("유레카 서버 상태 확인")
    void eurekaServerStatusTest() {
        // 유레카 서버 상태 엔드포인트 확인
        ResponseEntity<String> entity = restTemplate.getForEntity("/eureka/apps", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("유레카 서버 동작 확인")
    void eurekaServerHealthCheck() {
        // 유레카 서버 헬스 체크
        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("서비스 등록 및 발견 테스트")
    void serviceRegistrationAndDiscoveryTest() {
        // 서비스 목록 조회
        String url = "http://localhost:" + port + "/eureka/apps";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("메인 메소드 실행 테스트")
    void mainMethodTest() {
        // given
        String[] args = new String[]{};

        // when & then
        assertDoesNotThrow(() -> {
            EurekaServerApplication.main(args);
        });
    }

    @Test
    @DisplayName("애플리케이션 컨텍스트 로드 테스트")
    void contextLoads() {
    }

    @Test
    @DisplayName("유레카 서버 활성화 어노테이션 확인")
    void enableEurekaServerAnnotationTest() {
        // given
        EnableEurekaServer annotation = EurekaServerApplication.class.getAnnotation(EnableEurekaServer.class);

        // then
        assertNotNull(annotation);
    }
}