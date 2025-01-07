package com.sparta.userservice.controller;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.service.UserService;
import com.sparta.userservice.util.EncryptionUtil;
import com.sparta.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    private UserController userController;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisTokenRepository redisTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, userRepository, jwtUtil, redisTokenRepository, passwordEncoder, null);
    }

    @Nested
    @DisplayName("K6 테스트 임시사용자")
    class K6Tests {

        @DisplayName("K6 임시사용자 등록")
        @Test
        void testSignup() throws Exception {
            UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "username");
            when(userRepository.existsByUserEmail(anyString())).thenReturn(false);
            when(userService.createVerificationToken(any())).thenReturn("test-token");

            ResponseEntity<Map<String, String>> response = userController.signup(requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            assertThat(response.getBody()).containsKey("msg");
            verify(userService, times(1)).sendEmail(eq("test@example.com"), anyString(), anyString());
        }

        @DisplayName("K6 임시사용자 회원가입")
        @Test
        void testTestSignup() throws Exception {
            // Given
            String email = "test@example.com";
            String password = "password";
            String username = "testUser";

            Map<String, String> body = Map.of(
                    "email", email,
                    "password", password,
                    "username", username
            );

            // Mock 설정
            when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

            // When
            userController.testSignup(body);

            // Then
            verify(userRepository, times(1)).save(any(Member.class));
            verify(passwordEncoder, times(1)).encode(password);
        }

        @DisplayName("K6 임시사용자 로그인")
        @Test
        void testLogin() throws Exception {
            String email = "test@example.com";
            String password = "password";
            Member user = new Member();
            user.setUserEmail(EncryptionUtil.encrypt(email));
            user.setUserName(EncryptionUtil.encrypt("username"));

            when(userService.authenticate(email, password)).thenReturn(user);
            when(jwtUtil.generateToken(user)).thenReturn("test-token");

            ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsKeys("token", "msg", "userId", "userName");
        }

        @DisplayName("K6 임시사용자 삭제 성공")
        @Test
        void testDeleteUserSuccess() throws Exception {
            // Given
            String email = "test@example.com";

            // When
            userController.deleteUser(email);

            // Then
            verify(userRepository, times(1)).deleteByUserEmail(eq(EncryptionUtil.encrypt(email)));
        }
    }

    @Nested
    @DisplayName("일반 사용자")
    class normalUser {
        @Test
        @DisplayName("이메일 인증 성공")
        void testVerifyEmailSuccess() throws Exception {
            // Given
            String token = "valid-token";
            String email = "test@example.com";

            // Mock 설정
            doNothing().when(userRepository).updateStatusFindByEmail(anyString());

            // When
            ResponseEntity<String> response = userController.verifyEmail(token, email);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).isEqualTo("이메일 인증이 완료되었습니다!");
            verify(userRepository, times(1)).updateStatusFindByEmail(eq(EncryptionUtil.encrypt(email)));
        }

        @Test
        @DisplayName("회원가입 성공")
        void testSignupSuccess() throws Exception {
            // Given
            UserSignupRequestDto userRequest = new UserSignupRequestDto("test@example.com", "password", "username");
            when(userRepository.existsByUserEmail(anyString())).thenReturn(false);
            when(userService.createVerificationToken(any(UserSignupRequestDto.class))).thenReturn("test-token");

            // When
            ResponseEntity<Map<String, String>> response = userController.signup(userRequest);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(202);
            assertThat(response.getBody()).containsKey("msg");
            assertThat(response.getBody().get("msg")).contains("인증메일이 전송되었습니다.");
            verify(userRepository, times(1)).existsByUserEmail(anyString());
            verify(userService, times(1)).createVerificationToken(any(UserSignupRequestDto.class));
            verify(userService, times(1)).sendEmail(eq("test@example.com"), anyString(), anyString());
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 중복")
        void testSignupEmailConflict() throws Exception {
            // Given
            UserSignupRequestDto userRequest = new UserSignupRequestDto("duplicate@example.com", "password", "username");
            when(userRepository.existsByUserEmail(anyString())).thenReturn(true);

            // When
            ResponseEntity<Map<String, String>> response = userController.signup(userRequest);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(409);
            assertThat(response.getBody()).containsKey("msg");
            assertThat(response.getBody().get("msg")).isEqualTo("이미 사용된 이메일입니다.");
            verify(userRepository, times(1)).existsByUserEmail(anyString());
            verify(userService, never()).createVerificationToken(any(UserSignupRequestDto.class));
            verify(userService, never()).sendEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("로그인 실패 - 사용자 인증 실패")
        void testLoginFailure() throws Exception {
            // given
            Map<String, String> loginRequest = Map.of("email", "test@example.com", "password", "wrongPassword");
            when(userService.authenticate(anyString(), anyString())).thenReturn(null);

            // when
            ResponseEntity<Map<String, String>> response = userController.login(loginRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).containsEntry("msg", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        @DisplayName("로그인 성공")
        @Test
        void testLoginSuccess() throws Exception {
            // Given
            String email = "test@example.com";
            String password = "password";
            Member mockUser = new Member();
            mockUser.setUserEmail(EncryptionUtil.encrypt(email));
            mockUser.setUserName(EncryptionUtil.encrypt("testUser"));

            when(userService.authenticate(email, password)).thenReturn(mockUser);
            when(jwtUtil.generateToken(mockUser)).thenReturn("mockJwtToken");

            // When
            ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsKeys("token", "msg", "userId", "userName");
            assertThat(response.getBody().get("msg")).isEqualTo("로그인 성공!");
            assertThat(response.getBody().get("token")).isEqualTo("mockJwtToken");

            verify(userService, times(1)).authenticate(email, password);
            verify(jwtUtil, times(1)).generateToken(mockUser);
            verify(redisTokenRepository, times(1)).saveToken("mockJwtToken", email, 15 * 60 * 1000);
        }

        @DisplayName("현재 기기에서 로그아웃 테스트")
        @Test
        void testLogout() throws Exception {
            // Given
            String validToken = "valid-token";
            String invalidToken = "invalid-token";

            // Mock redisTokenRepository.removeToken 호출 결과
            when(redisTokenRepository.removeToken(validToken)).thenReturn(true);
            when(redisTokenRepository.removeToken(invalidToken)).thenReturn(false);

            // When - 유효한 토큰 로그아웃 요청
            ResponseEntity<String> validResponse = userController.logout(validToken);

            // Then - 유효한 토큰 로그아웃 성공 검증
            assertThat(validResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(validResponse.getBody()).isEqualTo("현재 기기에서 로그아웃되었습니다!");

            // When - 유효하지 않은 토큰 로그아웃 요청
            ResponseEntity<String> invalidResponse = userController.logout(invalidToken);

            // Then - 유효하지 않은 토큰 로그아웃 실패 검증
            assertThat(invalidResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(invalidResponse.getBody()).isEqualTo("유효하지 않은 토큰입니다.");

            // Verify - redisTokenRepository 호출 검증
            verify(redisTokenRepository, times(1)).removeToken(validToken);
            verify(redisTokenRepository, times(1)).removeToken(invalidToken);
        }

        @Test
        @DisplayName("전체 기기 로그아웃 - 유효한 토큰")
        void testLogoutAllWithValidToken() throws Exception {
            // Given
            when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
            when(redisTokenRepository.removeAllTokensByEmail(anyString())).thenReturn(3);

            // When
            ResponseEntity<String> response = userController.logoutAll("Bearer test-token");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("모든 기기에서 로그아웃되었습니다!");
        }

        @Test
        @DisplayName("전체 기기 로그아웃 - 유효하지 않은 토큰")
        void testLogoutAllWithInvalidToken() throws Exception {
            // Given
            when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
            when(redisTokenRepository.removeAllTokensByEmail(anyString())).thenReturn(0);

            // When
            ResponseEntity<String> response = userController.logoutAll("Bearer invalid-token");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isEqualTo("유효하지 않은 요청입니다.");
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void testUpdatePasswordSuccess() throws Exception {
            String email = "test@example.com";
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            Member user = new Member();

            // Mock 설정
            when(userService.authenticate(email, oldPassword)).thenReturn(user);
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
            when(redisTokenRepository.removeAllTokensByEmail(anyString())).thenReturn(1);

            // 테스트 실행
            ResponseEntity<String> response = userController.updatePassword(
                    "Bearer test-token",
                    Map.of("email", email, "oldPassword", oldPassword, "newPassword", newPassword, "confirmPassword", newPassword)
            );

            // 결과 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("비밀번호가 성공적으로 변경");
        }

        @Test
        @DisplayName("비밀번호 변경 실패 - 로그인 토큰 없음")
        void testUpdatePasswordWithoutToken() throws Exception {
            // 테스트 실행
            ResponseEntity<String> response = userController.updatePassword(
                    null,
                    Map.of("email", "test@example.com", "oldPassword", "oldPassword", "newPassword", "newPassword", "confirmPassword", "newPassword")
            );

            // 결과 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).contains("로그인해야 이용하실 수 있는 기능입니다");
        }

        @Test
        @DisplayName("비밀번호 변경 실패 - 인증 실패")
        void testUpdatePasswordAuthenticationFail() throws Exception {
            String email = "test@example.com";
            String oldPassword = "wrongPassword";

            // Mock 설정
            when(userService.authenticate(email, oldPassword)).thenReturn(null);

            // 테스트 실행
            ResponseEntity<String> response = userController.updatePassword(
                    "Bearer test-token",
                    Map.of("email", email, "oldPassword", oldPassword, "newPassword", "newPassword", "confirmPassword", "newPassword")
            );

            // 결과 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).contains("현재 비밀번호가 틀렸습니다");
        }

        @Test
        @DisplayName("비밀번호 변경 실패 - 비밀번호 불일치")
        void testUpdatePasswordMismatch() throws Exception {
            String email = "test@example.com";
            String oldPassword = "oldPassword";

            // Mock 설정
            when(userService.authenticate(email, oldPassword)).thenReturn(new Member());

            // 테스트 실행
            ResponseEntity<String> response = userController.updatePassword(
                    "Bearer test-token",
                    Map.of("email", email, "oldPassword", oldPassword, "newPassword", "newPassword", "confirmPassword", "differentPassword")
            );

            // 결과 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("변경을 원하는 비밀번호와 비밀번호 확인이 일치하지 않습니다");
        }

        @Test
        @DisplayName("비밀번호 변경 성공 - 로그아웃 실패")
        void testUpdatePasswordLogoutFail() throws Exception {
            String email = "test@example.com";
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            Member user = new Member();

            // Mock 설정
            when(userService.authenticate(email, oldPassword)).thenReturn(user);
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
            when(redisTokenRepository.removeAllTokensByEmail(anyString())).thenReturn(0);

            // 테스트 실행
            ResponseEntity<String> response = userController.updatePassword(
                    "Bearer test-token",
                    Map.of("email", email, "oldPassword", oldPassword, "newPassword", newPassword, "confirmPassword", newPassword)
            );

            // 결과 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).contains("비밀번호는 변경이 잘 되었는데요");
        }
    }
}
