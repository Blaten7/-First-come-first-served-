package com.sparta.userservice.service;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTokenRepository redisTokenRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private UserServiceImpl userService;

    private UserSignupRequestDto userRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserSignupRequestDto();
        userRequest.setUserEmail("test@example.com");
        userRequest.setUserName("testUser");
        userRequest.setUserPw("password123");
        userRequest.setUserAddress("서울시");
        userRequest.setUserPH("010-1234-5678");
        userRequest.setDescription("테스트 설명");
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void generateTokenTest() {
        // Given
        UUID uuid = UUID.randomUUID();
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(uuid);

            // When
            String token = userService.generateToken();

            // Then
            assertThat(token).isEqualTo(uuid.toString());
        }
    }

    @Test
    @DisplayName("토큰 생성 실패 테스트 - saveTempUser 예외")
    void createVerificationTokenFailure() throws Exception {
        // Given
        UserSignupRequestDto userRequest = new UserSignupRequestDto();
        userRequest.setUserEmail("test@example.com");
        userRequest.setUserName("testUser");
        userRequest.setUserPw("password");
        userRequest.setUserAddress("address");
        userRequest.setUserPH("010-1234-5678");

        try (MockedStatic<EncryptionUtil> encryptionUtil = mockStatic(EncryptionUtil.class)) {
            encryptionUtil.when(() -> EncryptionUtil.encrypt(anyString()))
                    .thenThrow(new Exception("암호화 실패"));

            // When
            String result = userService.createVerificationToken(userRequest);

            // Then
            assertThat(result).isNull();
            verify(userRepository, never()).save(any(Member.class));
        }
    }

    @Test
    @DisplayName("임시 사용자 저장 실패 - 암호화 예외")
    void saveTempUserEncryptionFailure() throws Exception {
        // Given
        UserSignupRequestDto userRequest = new UserSignupRequestDto();
        userRequest.setUserEmail("test@example.com");
        userRequest.setUserName("testUser");

        try (MockedStatic<EncryptionUtil> encryptionUtil = mockStatic(EncryptionUtil.class)) {
            encryptionUtil.when(() -> EncryptionUtil.encrypt(anyString()))
                    .thenThrow(new Exception("암호화 실패"));

            // When & Then
            assertThatThrownBy(() -> userService.saveTempUser(userRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessage("암호화 실패");
        }

        verify(userRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 - 메일 전송 예외")
    void sendEmailFailureMailException() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new MailSendException("메일 전송 실패"))
                .when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(() ->
                userService.sendEmail("test@example.com", "제목", "링크"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("메일 전송 실패");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 - 잘못된 이메일 주소")
    void sendEmailFailureInvalidEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new MailSendException("Invalid email address"))
                .when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(() ->
                userService.sendEmail("invalid-email", "제목", "링크"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email address");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 - MimeMessageHelper 생성 실패")
    void sendEmailFailureWithMessageException() {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // mailSender.send() 호출 시 RuntimeException 발생
        doAnswer(invocation -> {
            throw new MessagingException("메일 전송 실패");
        }).when(mailSender).send(any(MimeMessage.class));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.sendEmail("test@example.com", "테스트", "http://test.com"));

        assertThat(exception)
                .hasMessage("Failed to send email")
                .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 내용 검증")
    void verifyEmailContent() throws MessagingException {
        // Given
        String toEmail = "test@example.com";
        String subject = "테스트 제목";
        String verificationLink = "http://test.com/verify";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper mimeMessageHelper = mock(MimeMessageHelper.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        userService.sendEmail(toEmail, subject, verificationLink);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("인증 토큰 생성 성공 테스트")
    void createVerificationTokenSuccessTest() throws Exception {
        // Given
        String token = "test-token";
        when(userRepository.save(any(Member.class))).thenReturn(new Member());
        try (MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            mockedEncryption.when(() -> EncryptionUtil.encrypt(anyString())).thenReturn("encrypted");

            // When
            String result = userService.createVerificationToken(userRequest);

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("인증 성공 테스트")
    void authenticateSuccessTest() throws Exception {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        Member member = new Member();
        member.setUserPw(encodedPassword);

        try (MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            mockedEncryption.when(() -> EncryptionUtil.encrypt(anyString())).thenReturn("encrypted");
            when(userRepository.findByUserEmail("encrypted")).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            // When
            Member result = userService.authenticate("test@example.com", rawPassword);

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("인증 실패 테스트 - 잘못된 이메일")
    void authenticateFailureInvalidEmailTest() throws Exception {
        // Given
        try (MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            mockedEncryption.when(() -> EncryptionUtil.encrypt(anyString())).thenReturn("encrypted");
            when(userRepository.findByUserEmail("encrypted")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    userService.authenticate("test@example.com", "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 이메일이 아닙니다");
        }
    }

    @Test
    @DisplayName("인증 실패 테스트 - 잘못된 비밀번호")
    void authenticateFailureInvalidPasswordTest() throws Exception {
        // Given
        String rawPassword = "password123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = "encodedPassword";

        Member member = new Member();
        member.setUserPw(encodedPassword);

        try (MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            mockedEncryption.when(() -> EncryptionUtil.encrypt(anyString())).thenReturn("encrypted");
            when(userRepository.findByUserEmail("encrypted")).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(eq(wrongPassword), eq(encodedPassword))).thenReturn(false);

            // When & Then
            assertThatThrownBy(() ->
                    userService.authenticate("test@example.com", wrongPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 비밀번호가 아닙니다");
        }
    }
}