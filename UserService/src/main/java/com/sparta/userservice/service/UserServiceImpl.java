package com.sparta.userservice.service;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;
    private final JavaMailSender mailSender;

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void sendEmail(String toEmail, String subject, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 메일 내용 (HTML 형식)
            String htmlBody = "<p>아래의 링크를 클릭하여 인증을 진행해주세요!</p>"
                    + "<a href=\"" + verificationLink + "\">" + verificationLink + "</a>";

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true: HTML 내용으로 설정

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public String createVerificationToken(UserSignupRequestDto userRequest) {
        String token = generateToken();
        try {
            saveTempUser(userRequest);
            long TOKEN_EXPIRES_IN_MINUTES = 5 * 60 * 1000L;
            redisTokenRepository.saveToken(token, userRequest.getUserEmail(), TOKEN_EXPIRES_IN_MINUTES);
            return token; // 저장 성공 시 토큰 반환
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveTempUser(UserSignupRequestDto userRequest) throws Exception {
        Member user = new Member();
        user.setUserName(EncryptionUtil.encrypt(userRequest.getUserName()));
        user.setUserEmail(EncryptionUtil.encrypt(userRequest.getUserEmail()));
        user.setUserPw(passwordEncoder.encode(userRequest.getUserPw()));
        user.setUserAddress(EncryptionUtil.encrypt(userRequest.getUserAddress()));
        user.setUserPH(EncryptionUtil.encrypt(userRequest.getUserPH()));
        user.setProfileImg(userRequest.getProfileImg());
        user.setDescription(EncryptionUtil.encrypt(userRequest.getDescription()));
        user.setStatus("TEMP");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public Member authenticate(String email, String password) throws Exception {
        Optional<Member> optionalUser = userRepository.findByUserEmail(EncryptionUtil.encrypt(email));
        if (optionalUser.isPresent()) {
            Member user = optionalUser.get();

            // 비밀번호 검증
            if (passwordEncoder.matches(password, user.getUserPw())) {
                return user; // 로그인 성공, User 객체 반환
            }
            System.out.println("현재 비밀번호 : " + user.getUserPw());
            System.out.println("바꾸려는 비밀번호 : " + password);
            throw new IllegalArgumentException("유효한 비밀번호가 아닙니다");
        }
        throw new IllegalArgumentException("유효한 이메일이 아닙니다");
    }
}
