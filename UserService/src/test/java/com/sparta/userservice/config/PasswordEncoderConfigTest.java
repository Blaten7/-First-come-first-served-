package com.sparta.userservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Import(PasswordEncoderConfig.class)
class PasswordEncoderConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PasswordEncoder Bean 생성 테스트")
    void testPasswordEncoderBean() {
        // Then
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 암호화 테스트")
    void testPasswordEncoding() {
        // Given
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("서로 다른 비밀번호 암호화 결과 비교 테스트")
    void testDifferentEncodings() {
        // Given
        String password = "testPassword123";

        // When
        String firstEncoding = passwordEncoder.encode(password);
        String secondEncoding = passwordEncoder.encode(password);

        // Then
        assertThat(firstEncoding).isNotEqualTo(secondEncoding);
        assertThat(passwordEncoder.matches(password, firstEncoding)).isTrue();
        assertThat(passwordEncoder.matches(password, secondEncoding)).isTrue();
    }
}