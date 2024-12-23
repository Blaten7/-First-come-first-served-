//package com.sparta.userservice;
//
//import com.sparta.userservice.controller.UserController;
//import org.junit.jupiter.api.Test;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sparta.userservice.dto.UserSignupRequestDto;
//import com.sparta.userservice.entity.User;
//import com.sparta.userservice.repository.RedisTokenRepository;
//import com.sparta.userservice.repository.UserRepository;
//import com.sparta.userservice.repository.VerificationTokenRepository;
//import com.sparta.userservice.service.EmailService;
//import com.sparta.userservice.service.UserService;
//import com.sparta.userservice.util.JwtUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootApplication
//class UserServiceApplicationTests {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private EmailService emailService;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private VerificationTokenRepository vtRepository;
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired
//    private RedisTokenRepository redisTokenRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testSignup_Success() throws Exception {
//        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "testUser");
//        when(userRepository.existsByUserEmail(anyString())).thenReturn(false);
//        when(userService.createVerificationToken(any())).thenReturn("testToken");
//
//        mockMvc.perform(post("/api/user/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.msg").value("test@example.com 으로 인증메일이 전송되었습니다. 메일을 확인해주세요."));
//
//        verify(emailService, times(1)).sendEmail(eq("test@example.com"), anyString(), anyString());
//    }
//
//    @Test
//    void testSignup_EmailAlreadyExists() throws Exception {
//        UserSignupRequestDto requestDto = new UserSignupRequestDto("test@example.com", "password", "testUser");
//        when(userRepository.existsByUserEmail(anyString())).thenReturn(true);
//
//        mockMvc.perform(post("/api/user/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.msg").value("이미 사용된 이메일입니다."));
//
//        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
//    }
//
//    @Test
//    void testVerifyEmail_Success() throws Exception {
//        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(1);
//
//        mockMvc.perform(get("/api/user/auth/verify")
//                        .param("token", "validToken")
//                        .param("email", "test@example.com"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("이메일 인증이 완료되었습니다!"));
//
//        verify(userRepository, times(1)).updateStatusFindByEmail(anyString());
//    }
//
//    @Test
//    void testVerifyEmail_InvalidToken() throws Exception {
//        when(vtRepository.countByTokenAndExpiryDateAfter(anyString())).thenReturn(0);
//
//        mockMvc.perform(get("/api/user/auth/verify")
//                        .param("token", "invalidToken")
//                        .param("email", "test@example.com"))
//                .andExpect(status().isForbidden())
//                .andExpect(content().string("유효하지 않은 토큰입니다."));
//    }
//
//    @Test
//    void testLogin_Success() throws Exception {
//        User mockUser = new User("test@example.com", "password", "testUser");
//        when(userService.authenticate(anyString(), anyString())).thenReturn(mockUser);
//        when(jwtUtil.generateToken(any())).thenReturn("jwtToken");
//
//        mockMvc.perform(post("/api/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "password"))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value("jwtToken"))
//                .andExpect(jsonPath("$.msg").value("로그인 성공!"));
//
//        verify(redisTokenRepository, times(1)).saveToken(eq("jwtToken"), eq("test@example.com"), anyLong());
//    }
//
//    @Test
//    void testLogin_InvalidCredentials() throws Exception {
//        when(userService.authenticate(anyString(), anyString())).thenReturn(null);
//
//        mockMvc.perform(post("/api/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "wrongPassword"))))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.msg").value("로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."));
//    }
//}
