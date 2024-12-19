package com.sparta.webapi.controller;

import com.sparta.application.service.EmailService;
import com.sparta.application.service.UserService;
import com.sparta.domain.dto.UserSignupRequestDto;
import com.sparta.domain.repository.UserRepository;
import com.sparta.domain.repository.VerificationTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository vtRepository;

    public UserController(EmailService emailService, UserService userService, UserRepository userRepository, VerificationTokenRepository vtRepository) {
        this.emailService = emailService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.vtRepository = vtRepository;
    }

    @Operation(summary = "회원가입 - 이메일 인증", description = "사용자가 이메일을 통해 회원가입을 진행합니다.")
    @ApiResponse(responseCode = "202", description = "인증메일 전송 성공")
    @PostMapping("/user/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody UserSignupRequestDto userRequest) {
        log.info("회원가입 - 이메일 인증 컨트롤러 진입");
        String email = userRequest.getUserEmail();
        if (userRepository.existsByUserEmail(email)) return ResponseEntity.status(409).body(Map.of("msg", "이미 사용된 이메일입니다."));
        // 토큰을 토큰 저장소에 저장하고 유저정보를 임시 저장.
        String token = userService.createVerificationToken(userRequest);
        String subject = "회원가입 인증 이메일!";
        String body = "http://localhost:8084/api/auth/verify?token='" + token + "'&email=" + email;
        emailService.sendEmail(email, subject, body);
        return ResponseEntity
                .status(202)
                .body(Map.of("msg",
                        String.format(
                                "%s 으로 인증메일이 전송되었습니다. 메일을 확인해주세요.",
                                userRequest.getUserEmail())));
    }

    @Operation(summary = "이메일 인증 확인", description = "이메일 인증을 확인하는 엔드포인트")
    @GetMapping("/auth/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token, @RequestParam String email) {
        // 토큰 검증 로직
        if (isValidToken(token)) {
            userRepository.updateStatusFindByEmail(email);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
        }
        return ResponseEntity.status(403).body("유효하지 않은 토큰입니다.");
    }

    private boolean isValidToken(String token) {
        if (vtRepository.findByTokenAndExpiryDateAfter(token, LocalDateTime.now())) return true;
        return false;
    }

    @Operation(summary = "로그인", description = "JWT 토큰을 이용한 로그인 기능")
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        return Map.of("token", "jwt-token-string", "msg", "로그인 성공!");
    }

    @Operation(summary = "로그아웃", description = "현재 기기에서 로그아웃")
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("msg", "로그아웃 성공!");
    }

    @Operation(summary = "모든 기기에서 로그아웃", description = "모든 기기에서 로그아웃")
    @PostMapping("/logout/all")
    public Map<String, String> logoutAll() {
        return Map.of("msg", "모든 장치에서 로그아웃 되었습니다!");
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경하며 모든 기기에서 로그아웃")
    @PutMapping("/password")
    public Map<String, String> updatePassword(@RequestBody Map<String, String> passwordRequest) {
        return Map.of("msg", "비밀번호가 변경되었습니다. 모든 장치에서 로그아웃됩니다");
    }
}
