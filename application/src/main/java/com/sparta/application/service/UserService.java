package com.sparta.application.service;

import com.sparta.domain.dto.UserSignupRequestDto;
import com.sparta.domain.entity.User;
import com.sparta.domain.entity.VerificationToken;
import com.sparta.domain.repository.UserRepository;
import com.sparta.domain.repository.VerificationTokenRepository;
import com.sparta.domain.service.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserService {
    private final TokenService tokenService;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

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

    private void saveTempUser(UserSignupRequestDto userRequest) {
        User user = new User();
        user.setUserName(userRequest.getUserName());
        user.setUserEmail(userRequest.getUserEmail());
        user.setUserPw(userRequest.getUserPw());
        user.setUserPH(userRequest.getUserPH());
        user.setProfileImg(userRequest.getProfileImg());
        user.setDescription(userRequest.getDescription());
        user.setStatus("TEMP");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}
