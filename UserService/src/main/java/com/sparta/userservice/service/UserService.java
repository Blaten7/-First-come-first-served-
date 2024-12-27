package com.sparta.userservice.service;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.entity.VerificationToken;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.repository.VerificationTokenRepository;
import com.sparta.userservice.util.EncryptionUtil;
import lombok.AllArgsConstructor;
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
