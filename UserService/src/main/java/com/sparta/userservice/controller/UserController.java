package com.sparta.userservice.controller;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.service.EmailService;
import com.sparta.userservice.service.UserService;
import com.sparta.userservice.util.EncryptionUtil;
import com.sparta.userservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTokenRepository redisTokenRepository;
    private static final long TOKEN_EXPIRATION_TIME = 15 * 60 * 1000; // 15분
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/check-login")
    public String checkLogin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "로그인된 사용자: " + authentication.getName();
        } else {
            return "로그인되지 않은 사용자";
        }
    }

    @DeleteMapping("/delete/ALL")
    public Mono<Void> deleteAll() {
        return userRepository.deleteAll();
    }

    @Operation(summary = "회원가입 - 이메일 인증", description = "사용자가 이메일을 통해 회원가입을 진행합니다.")
    @ApiResponse(responseCode = "202", description = "인증메일 전송 성공")
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, String>>> signup(@RequestBody UserSignupRequestDto userRequest) throws Exception {
        log.info("회원가입 - 이메일 인증 컨트롤러 진입");
        String email = userRequest.getUserEmail();

        return userRepository.existsByUserEmail(EncryptionUtil.encrypt(email))
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.just(ResponseEntity.status(400).body(Map.of("msg", "이미 존재하는 이메일입니다.")));
                    }

                    String token = jwtUtil.generateTempToken(email);

                    // saveTempToken이 완료된 후에 saveTempUser 및 이메일 전송 로직을 실행
                    try {
                        return redisTokenRepository.saveTempToken(token, email)
                                .then(userService.saveTempUser(userRequest)) // saveTempUser 호출
                                .then(Mono.defer(() -> {
                                    String subject = "회원가입 인증 이메일!";
                                    String body = "http://localhost:8222/api/user/auth/verify?token=" + token + "&email=" + email;
                                    emailService.sendEmail(email, subject, body);

                                    return Mono.just(ResponseEntity.status(202).body(Map.of(
                                            "msg", String.format("%s 으로 인증메일이 전송되었습니다. 메일을 확인해주세요.", email)
                                    )));
                                }))
                                .onErrorResume(e -> {
                                    log.error("회원가입 처리 중 에러 발생: {}", e.getMessage());
                                    return Mono.just(ResponseEntity.status(500).body(Map.of("msg", "회원가입 처리 중 문제가 발생했습니다.")));
                                });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }



    @Operation(summary = "이메일 인증 확인", description = "이메일 인증을 확인하는 엔드포인트")
    @GetMapping("/auth/verify")
    public Mono<ResponseEntity<String>> verifyEmail(@RequestParam String token, @RequestParam String email){
        log.info("이메일 검증 컨트롤러 진입");

        // EncryptionUtil.encrypt(email) 예외 처리 추가
        return Mono.fromCallable(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email cannot be null or blank");
            }
            return EncryptionUtil.encrypt(email);
        }).flatMap(encryptedEmail -> {
            log.debug("Encrypted email: {}", encryptedEmail);

            return redisTokenRepository.isTempTokenValid(token, email)
                    .flatMap(isValid -> {
                        if (isValid) {
                            log.info("Token is valid. Updating user status...");
                            return userRepository.updateStatusFindByEmail(encryptedEmail)
                                    .thenReturn(ResponseEntity.ok("이메일 인증이 완료되었습니다!"));
                        } else {
                            log.warn("Invalid token or email. Deleting temporary user...");
                            return userRepository.deleteByUserEmail(encryptedEmail)
                                    .thenReturn(ResponseEntity.status(403).body("유효하지 않은 토큰입니다."));
                        }
                    });
        }).onErrorResume(e -> {
            log.error("Error during email verification", e);
            return Mono.just(ResponseEntity.status(500).body("서버 내부 오류: " + e.getMessage()));
        });
    }

    @PostMapping("/isValid")
    public Mono<Boolean> isValidTokenFromOrderService(@RequestParam String token) {
        log.info("로그인 토큰 검증 컨트롤러 진입 from OtherService");
        return redisTokenRepository.isTokenValid(token);
    }

    @Operation(summary = "로그인", description = "JWT 토큰을 이용한 로그인 기능")
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody Map<String, String> loginRequest, ServerHttpResponse response) throws Exception {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        return userService.authenticate(email, password)
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    ResponseCookie cookie = ResponseCookie.from("token", token)
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(15 * 60)
                            .build();
                    response.addCookie(cookie);
                    return ResponseEntity.ok(Map.of("token", token, "msg", "로그인 성공!"));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("msg", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.")));
    }

    @Operation(summary = "현재 기기에서 로그아웃", description = "현재 사용 중인 기기에서 로그아웃")
    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String token, ServerHttpResponse response) {
        log.info("현재 기기 로그아웃 요청 처리 컨트롤러 진입");
        return redisTokenRepository.addToBlacklist(token, TOKEN_EXPIRATION_TIME)
                .then(Mono.fromRunnable(() -> {
                    ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(0)
                            .build();
                    response.addCookie(deleteCookie);
                })).thenReturn(ResponseEntity.ok("현재 기기에서 로그아웃되었습니다!"));
    }

    @Operation(summary = "모든 기기에서 로그아웃", description = "사용자의 모든 기기에서 로그아웃")
    @PostMapping("/logout/all")
    public Mono<ResponseEntity<String>> logoutAll(@RequestHeader("Authorization") String token) {
        log.info("전체 기기 로그아웃 요청 처리 컨트롤러 진입");
        String tokens = token.replace("Bearer ", "");

        return Mono.fromCallable(() -> jwtUtil.extractEmail(tokens)) // 동기 메서드를 비동기 체인으로 감싸기
                .flatMap(email -> redisTokenRepository.addToBlacklist(tokens, TOKEN_EXPIRATION_TIME)
                        .then(Mono.just(ResponseEntity.status(HttpStatus.OK).body("모든 기기에서 로그아웃되었습니다.")))
                )
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 요청입니다.")));
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경하며 모든 기기에서 로그아웃")
    @PutMapping("/password/change/request")
    public Mono<ResponseEntity<String>> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> pwChangeRequest) throws Exception {

        log.info("비밀번호 변경 컨트롤러 진입");

        String email = pwChangeRequest.get("email");
        String oldPassword = pwChangeRequest.get("oldPassword");
        String newPassword = pwChangeRequest.get("newPassword");
        String confirmPassword = pwChangeRequest.get("confirmPassword");

        return userService.authenticate(email, oldPassword)
                .flatMap(user -> {
                    if (!newPassword.equals(confirmPassword)) {
                        return Mono.just(ResponseEntity.status(400)
                                .body("변경을 원하는 비밀번호와 비밀번호 확인이 일치하지 않습니다."));
                    }
                    // EncryptionUtil.encrypt(email)을 비동기적으로 처리
                    return Mono.fromCallable(() -> EncryptionUtil.encrypt(email))
                            .flatMap(encryptedEmail -> userRepository.updateUserPwAndPwUpdatedAtByUserEmail(
                                            encryptedEmail,
                                            passwordEncoder.encode(newPassword))
                                    .then(redisTokenRepository.removeAllTokensByEmail(encryptedEmail))
                                    .flatMap(removedTokens -> {
                                        if (removedTokens > 0) {
                                            return Mono.just(ResponseEntity.ok(
                                                    "비밀번호가 성공적으로 변경되어 모든 기기에서 로그아웃 되었습니다.\n새로운 비밀번호를 사용하여 로그인 해주세요!"));
                                        }
                                        return Mono.just(ResponseEntity.status(422)
                                                .body("비밀번호는 변경되었지만 모든 기기에서 로그아웃은 실패했습니다."));
                                    }));
                })
                .defaultIfEmpty(ResponseEntity.status(401)
                        .body("유효하지 않은 요청입니다. 로그인 후 다시 시도해주세요."));
    }

    @Operation(summary = "로그인 검증", description = "다른 서비스에서 로그인 여부 확인시 사용")
    @PostMapping("/isValid/token")
    public Mono<Boolean> isValidJWTToken(@RequestParam String token) {
        return redisTokenRepository.isTokenValid(token);
    }
}
