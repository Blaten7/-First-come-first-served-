package com.sparta.application.service;

import com.sparta.domain.dto.UserSignupRequestDto;
import com.sparta.domain.util.EncryptionUtil;
import com.sparta.domain.entity.User;
import com.sparta.domain.entity.VerificationToken;
import com.sparta.domain.repository.UserRepository;
import com.sparta.domain.repository.VerificationTokenRepository;
import com.sparta.domain.service.TokenService;
import com.sparta.domain.util.EncryptionUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final TokenService tokenService;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String createVerificationToken(UserSignupRequestDto userRequest) {
        String token = tokenService.generateToken();

        // 이메일 및 토큰을 DB에 임시 저장
        VerificationToken verificationToken = new VerificationToken(
                token,
                userRequest.getUserEmail(),
                LocalDateTime.now().plusMinutes(5)
        );
        try {
            tokenRepository.save(verificationToken);
            saveTempUser(userRequest);
            return token; // 저장 성공 시 토큰 반환
        } catch (Exception e) {
            // 저장 실패 시 예외 처리
            return null;
        }
    }

    private void saveTempUser(UserSignupRequestDto userRequest) throws Exception {
        User user = new User();
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

    public User authenticate(String email, String password) throws Exception {
        Optional<User> optionalUser = userRepository.findByUserEmail(EncryptionUtil.encrypt(email));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 비밀번호 검증
            if (passwordEncoder.matches(password, user.getUserPw())) {
                return user; // 로그인 성공, User 객체 반환
            }
        }
        throw new IllegalArgumentException("Invalid email or password");
    }

}
