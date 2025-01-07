package com.sparta.userservice.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("WebExchangeBindException 처리 테스트")
    void handleValidationExceptions() {
        // Given
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();

        fieldErrors.add(new FieldError("objectName", "email", "이메일 형식이 올바르지 않습니다."));
        fieldErrors.add(new FieldError("objectName", "password", "비밀번호는 8자 이상이어야 합니다."));

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("email", "이메일 형식이 올바르지 않습니다.");
        assertThat(response.getBody()).containsEntry("password", "비밀번호는 8자 이상이어야 합니다.");
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("빈 필드 에러 처리 테스트")
    void handleEmptyValidationErrors() {
        // Given
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEmpty();
    }
}