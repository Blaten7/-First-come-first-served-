//package com.sparta.userservice.controller;
//
//import com.sparta.userservice.component.LoggingFilter;
//import com.sparta.userservice.config.PasswordEncoderConfig;
//import com.sparta.userservice.dto.UserSignupRequestDto;
//import com.sparta.userservice.entity.User;
//import com.sparta.userservice.entity.VerificationToken;
//import com.sparta.userservice.handler.GlobalExceptionHandler;
//import com.sparta.userservice.repository.RedisTokenRepository;
//import com.sparta.userservice.repository.UserRepository;
//import com.sparta.userservice.repository.VerificationTokenRepository;
//import com.sparta.userservice.service.EmailService;
//import com.sparta.userservice.service.UserService;
//import com.sparta.userservice.util.JwtUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.support.WebExchangeBindException;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//class UserServiceApplicationTests {
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private VerificationTokenRepository vtRepository;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @Mock
//    private RedisTokenRepository redisTokenRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    LoggingFilter loggingFilter;
//
//    @InjectMocks
//    private UserController userController;
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testSignupSuccess() throws Exception {
//        // Given
//        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "username");
//        when(userRepository.existsByUserEmail(anyString())).thenReturn(false);
//        when(userService.createVerificationToken(any())).thenReturn("test-token");
//
//        // When
//        ResponseEntity<Map<String, String>> response = userController.signup(requestDto);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(202);
//        assertThat(response.getBody()).containsKey("msg");
//        verify(emailService, times(1)).sendEmail(eq("test@example.com"), anyString(), anyString());
//    }
//
//    @Test
//    void testSignupDuplicateEmail() throws Exception {
//        // Given
//        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "username");
//        when(userRepository.existsByUserEmail(anyString())).thenReturn(true);
//
//        // When
//        ResponseEntity<Map<String, String>> response = userController.signup(requestDto);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(409);
//        assertThat(response.getBody()).containsEntry("msg", "이미 사용된 이메일입니다.");
//    }
//
//    @Test
//    void testVerifyEmailSuccess() throws Exception {
//        String token = "test-token";
//        String email = "test@example.com";
//        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(1L);
//
//        ResponseEntity<String> response = userController.verifyEmail(token, email);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody()).isEqualTo("이메일 인증이 완료되었습니다!");
//        verify(userRepository, times(1)).updateStatusFindByEmail(anyString());
//    }
//
//    @Test
//    void testVerifyEmailInvalidToken() throws Exception {
//        // Given
//        String token = "invalid-token";
//        String email = "test@example.com";
//        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(0L);
//
//        // When
//        ResponseEntity<String> response = userController.verifyEmail(token, email);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(403);
//        assertThat(response.getBody()).isEqualTo("유효하지 않은 토큰입니다.");
//    }
//
//    @Test
//    void testLoginSuccess() throws Exception {
//        // Given
//        String email = "test@example.com";
//        String password = "password";
//        User user = new User();
//        when(userService.authenticate(anyString(), anyString())).thenReturn(user);
//        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
//
//        // When
//        ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody()).containsKey("token");
//        assertThat(response.getBody()).containsEntry("msg", "로그인 성공!");
//        verify(redisTokenRepository, times(1)).saveToken(anyString(), eq(email), anyLong());
//    }
//
//    @Test
//    void testLoginFailure() throws Exception {
//        // Given
//        String email = "test@example.com";
//        String password = "wrong-password";
//        when(userService.authenticate(anyString(), anyString())).thenReturn(null);
//
//        // When
//        ResponseEntity<Map<String, String>> response = userController.login(Map.of("email", email, "password", password));
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(401);
//        assertThat(response.getBody()).containsEntry("msg", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.");
//    }
//
//    @Test
//    void testLogoutSuccess() {
//        // Given
//        String token = "Bearer jwt-token";
//        when(redisTokenRepository.isTokenValid(anyString())).thenReturn(true);
//        when(redisTokenRepository.removeToken(anyString())).thenReturn(true);
//
//        // When
//        ResponseEntity<String> response = userController.logout(token);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody()).isEqualTo("현재 기기에서 로그아웃되었습니다!");
//    }
//
//    @Test
//    void testLogoutInvalidToken() {
//        // Given
//        String token = "Bearer invalid-token";
//        when(redisTokenRepository.isTokenValid(anyString())).thenReturn(false);
//
//        // When
//        ResponseEntity<String> response = userController.logout(token);
//
//        // Then
//        assertThat(response.getStatusCodeValue()).isEqualTo(422);
//        assertThat(response.getBody()).isEqualTo("유효하지 않은 토큰입니다.");
//    }
//
//    @Test
//    void testFilter() {
//        // Arrange
//        LoggingFilter loggingFilter = new LoggingFilter();
//        ServerWebExchange mockExchange = mock(ServerWebExchange.class);
//        ServerHttpRequest mockRequest = mock(ServerHttpRequest.class);
//        WebFilterChain mockChain = mock(WebFilterChain.class);
//
//        when(mockExchange.getRequest()).thenReturn(mockRequest);
//        when(mockRequest.getURI()).thenReturn(java.net.URI.create("http://localhost/test"));
//        when(mockChain.filter(mockExchange)).thenReturn(Mono.empty());
//
//        // Act
//        Mono<Void> result = loggingFilter.filter(mockExchange, mockChain);
//
//        // Assert
//        result.block();
//        verify(mockRequest, times(1)).getURI();
//        verify(mockChain, times(1)).filter(mockExchange);
//    }
//
//    @Test
//    void testPasswordEncoderBean() {
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PasswordEncoderConfig.class);
//        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
//        assertThat(passwordEncoder).isNotNull();
//    }
//
//    @Test
//    void testUserEntity() {
//        User user = new User();
//        user.setUserId(1L);
//        user.setUserName("John");
//        user.setUserEmail("john@example.com");
//
//        assertThat(user.getUserId()).isEqualTo(1L);
//        assertThat(user.getUserName()).isEqualTo("John");
//        assertThat(user.getUserEmail()).isEqualTo("john@example.com");
//    }
//
//    @Test
//    void testVerificationTokenEntity() {
//        VerificationToken token = new VerificationToken();
//        token.setToken("test-token");
//        token.setUserEmail("test@example.com");
//        token.setExpiryDate(LocalDateTime.now());
//
//        assertThat(token.getToken()).isEqualTo("test-token");
//        assertThat(token.getUserEmail()).isEqualTo("test@example.com");
//    }
//
//    @Test
//    void testHandleValidationExceptions() {
//        GlobalExceptionHandler handler = new GlobalExceptionHandler();
//        WebExchangeBindException mockException = mock(WebExchangeBindException.class);
//
//        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(mockException);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(400);
//        assertThat(response.getBody()).isNotNull();
//    }
//
//    @Test
//    void testSaveToken() {
//        StringRedisTemplate mockTemplate = mock(StringRedisTemplate.class);
//        RedisTokenRepository repository = new RedisTokenRepository(mockTemplate);
//
//        repository.saveToken("test-token", "user@example.com", 1000L);
//        verify(mockTemplate.opsForValue(), times(1)).set(anyString(), eq("test-token"), eq(1000L), any());
//    }
//
//    @Test
//    void testGenerateToken() {
//        JwtUtil jwtUtil = new JwtUtil();
//        User user = new User();
//        user.setUserEmail("test@example.com");
//        String token = jwtUtil.generateToken(user);
//
//        assertThat(token).isNotNull();
//    }
//
//    @Test
//    void testExtractEmail() {
//        JwtUtil jwtUtil = new JwtUtil();
//        String email = jwtUtil.extractEmail("jwt-token");
//        assertThat(email).isEqualTo("test@example.com");
//    }
//
//}
