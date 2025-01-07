package com.sparta.productservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class ProductServiceApplicationTests {

    private static final String TEST_ENV_CONTENT = """
            TEST_KEY1=test_value1
            TEST_KEY2=test_value2
            """;

    private Path resourcesPath;
    private Path envFile;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 시스템 프로퍼티 클리어
        System.clearProperty("TEST_KEY1");
        System.clearProperty("TEST_KEY2");

        // 실제 애플리케이션과 동일한 경로 설정
        resourcesPath = Paths.get("ProductService", "src", "main", "resources");
        envFile = resourcesPath.resolve(".env");
    }

    @Test
    @DisplayName("스프링 컨텍스트 로드 확인")
    void shouldLoadEnvironmentVariablesAndStartApplication() throws IOException {
        // Given
        // Spring 애플리케이션 실행을 모의화
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            // 기존 .env 파일 백업
            Path backupFile = null;
            if (Files.exists(envFile)) {
                backupFile = envFile.getParent().resolve(".env.backup");
                Files.move(envFile, backupFile);
            }

            try {
                // 테스트용 .env 파일 생성
                Files.createDirectories(resourcesPath);
                Files.writeString(envFile, TEST_ENV_CONTENT);

                // When
                ProductServiceApplication.main(new String[]{});

                // Then
                // 1. 환경 변수가 정상적으로 로드되었는지 확인
                assertEquals("test_value1", System.getProperty("TEST_KEY1"));
                assertEquals("test_value2", System.getProperty("TEST_KEY2"));

                // 2. SpringApplication.run이 호출되었는지 확인
                mockedSpringApplication.verify(
                        () -> SpringApplication.run(
                                ProductServiceApplication.class,
                                new String[]{}
                        ),
                        times(1)
                );

            } finally {
                // 테스트 후 정리
                Files.deleteIfExists(envFile);

                // 백업 파일이 있었다면 복원
                if (backupFile != null && Files.exists(backupFile)) {
                    Files.move(backupFile, envFile);
                }
            }
        }
    }

}
