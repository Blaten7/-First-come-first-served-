package com.sparta.userservice.controller;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.User;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.repository.VerificationTokenRepository;
import com.sparta.userservice.service.EmailService;
import com.sparta.userservice.service.UserService;
import com.sparta.userservice.util.EncryptionUtil;
import com.sparta.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository vtRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignupSuccess() throws Exception {
        // Given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "username");
        when(userRepository.existsByUserEmail(anyString())).thenReturn(false);
        when(userService.createVerificationToken(any())).thenReturn("test-token");

        // When
        ResponseEntity<Map<String, String>> response = userController.signup(requestDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(202);
        assertThat(response.getBody()).containsKey("msg");
        verify(emailService, times(1)).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void testSignupDuplicateEmail() throws Exception {
        // Given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "username");
        when(userRepository.existsByUserEmail(anyString())).thenReturn(true);

        // When
        ResponseEntity<Map<String, String>> response = userController.signup(requestDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("msg", "이미 사용된 이메일입니다.");
    }

    @Test
    void testVerifyEmailSuccess() throws Exception {
        // Given
        String token = "test-token";
        String email = "test@example.com";
        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(1L);

        // When
        ResponseEntity<String> response = userController.verifyEmail(token, email);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("이메일 인증이 완료되었습니다!");
        verify(userRepository, times(1)).updateStatusFindByEmail(anyString());
    }

    @Test
    void testVerifyEmailInvalidToken() throws Exception {
        // Given
        String token = "invalid-token";
        String email = "test@example.com";
        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(0L);

        // When
        ResponseEntity<String> response = userController.verifyEmail(token, email);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        assertThat(response.getBody()).isEqualTo("유효하지 않은 토큰입니다.");
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        when(userService.authenticate(anyString(), anyString())).thenReturn(user);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody()).containsEntry("msg", "로그인 성공!");
        verify(redisTokenRepository, times(1)).saveToken(anyString(), eq(email), anyLong());
    }

    @Test
    void testLoginFailure() throws Exception {
        // Given
        String email = "test@example.com";
        String password = "wrong-password";
        when(userService.authenticate(anyString(), anyString())).thenReturn(null);

        // When
        ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).containsEntry("msg", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void testLogoutSuccess() {
        // Given
        String token = "Bearer jwt-token";
        when(redisTokenRepository.isTokenValid(anyString())).thenReturn(true);
        when(redisTokenRepository.removeToken(anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = userController.logout(token);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("현재 기기에서 로그아웃되었습니다!");
    }

    @Test
    void testLogoutInvalidToken() {
        // Given
        String token = "Bearer invalid-token";
        when(redisTokenRepository.isTokenValid(anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = userController.logout(token);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        assertThat(response.getBody()).isEqualTo("유효하지 않은 토큰입니다.");
    }
}
