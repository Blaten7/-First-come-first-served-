package com.sparta.userservice.service;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.util.EncryptionUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserService {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Void> saveTempUser(UserSignupRequestDto userRequest) throws Exception {
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
        return userRepository.save(user).then();
    }


    public Mono<Member> authenticate(String email, String password) throws Exception {
        return userRepository.findByUserEmail(EncryptionUtil.encrypt(email)) // 리액티브 메서드 호출
                .switchIfEmpty(Mono.error(new IllegalArgumentException("유효한 이메일이 아닙니다")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getUserPw())) {
                        return Mono.just(user); // 비밀번호가 일치하면 User 반환
                    } else {
                        System.out.println("현재 비밀번호 : " + user.getUserPw());
                        System.out.println("바꾸려는 비밀번호 : " + password);
                        return Mono.error(new IllegalArgumentException("유효한 비밀번호가 아닙니다"));
                    }
                });
    }




}
