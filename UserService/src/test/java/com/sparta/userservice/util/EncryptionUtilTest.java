package com.sparta.userservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionUtilTest {

    @Test
    @DisplayName("암호화/복호화 성공 테스트")
    void encryptDecryptSuccess() throws Exception {
        // Given
        String originalData = "테스트 데이터";

        // When
        String encrypted = EncryptionUtil.encrypt(originalData);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(originalData);
        assertThat(encrypted).isNotEqualTo(originalData);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("암호화 실패 - 빈 데이터")
    void encryptFailureEmptyData(String invalidData) {
        assertThatThrownBy(() -> EncryptionUtil.encrypt(invalidData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("암호화할 데이터가 없습니다");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("복호화 실패 - 빈 데이터")
    void decryptFailureEmptyData(String invalidData) {
        assertThatThrownBy(() -> EncryptionUtil.decrypt(invalidData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("복호화할 데이터가 없습니다");
    }

    @Test
    @DisplayName("복호화 실패 - 잘못된 암호화 데이터")
    void decryptFailureInvalidData() {
        assertThatThrownBy(() -> EncryptionUtil.decrypt("잘못된데이터"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("특수 문자 포함 데이터 암호화/복호화 테스트")
    void encryptDecryptSpecialCharacters() throws Exception {
        // Given
        String originalData = "!@#$%^&*()_+한글English123";

        // When
        String encrypted = EncryptionUtil.encrypt(originalData);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(originalData);
    }

    @Test
    @DisplayName("긴 문자열 암호화/복호화 테스트")
    void encryptDecryptLongString() throws Exception {
        // Given
        String originalData = "a".repeat(1000);

        // When
        String encrypted = EncryptionUtil.encrypt(originalData);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(originalData);
    }
}