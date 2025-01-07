package com.sparta.userservice.controller;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;
import com.sparta.userservice.repository.RedisTokenRepository;
import com.sparta.userservice.repository.UserRepository;
import com.sparta.userservice.service.UserService;
import com.sparta.userservice.util.EncryptionUtil;
import com.sparta.userservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTokenRepository redisTokenRepository;
    private static final long TOKEN_EXPIRATION_TIME = 15 * 60 * 1000; // 15분
    private final PasswordEncoder passwordEncoder;
    private Environment env;

    @PostMapping("/k6/test/signup")
    public void testSignup(@RequestBody Map<String, String> body) throws Exception {
        String email = body.get("email");
        String password = body.get("password");
        String username = body.get("username");
        Member member = new Member();
        member.setUserName(EncryptionUtil.encrypt(username));
        member.setUserEmail(EncryptionUtil.encrypt(email));
        member.setUserPw(passwordEncoder.encode(password));
        userRepository.save(member);
    }

    @DeleteMapping("/k6/test/deleteUser")
    public void deleteUser(@RequestParam String email) throws Exception {
        userRepository.deleteByUserEmail(EncryptionUtil.encrypt(email));
    }

    @Operation(summary = "회원가입 - 이메일 인증", description = "사용자가 이메일을 통해 회원가입을 진행합니다.")
    @ApiResponse(responseCode = "202", description = "인증메일 전송 성공")
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody UserSignupRequestDto userRequest) throws Exception {
        log.info("회원가입 - 이메일 인증 컨트롤러 진입");
        String email = userRequest.getUserEmail();
        if (userRepository.existsByUserEmail(EncryptionUtil.encrypt(email)))
            return ResponseEntity.status(409).body(Map.of("msg", "이미 사용된 이메일입니다."));
        // 토큰을 토큰 저장소에 저장하고 유저정보를 임시 저장.
        String token = userService.createVerificationToken(userRequest);
        String subject = "회원가입 인증 이메일!";
        String body = "http://localhost:8222/api/user/auth/verify?token='" + token + "'&email=" + email;
        userService.sendEmail(email, subject, body);
        return ResponseEntity
                .status(202)
                .body(Map.of("msg",
                        String.format(
                                "%s 으로 인증메일이 전송되었습니다. 메일을 확인해주세요.",
                                userRequest.getUserEmail())));
    }

    @Operation(summary = "이메일 인증 확인", description = "이메일 인증을 확인하는 엔드포인트")
    @GetMapping("/auth/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token, @RequestParam String email) throws Exception {
        log.info("이메일 검증 컨트롤러 진입");
        userRepository.updateStatusFindByEmail(EncryptionUtil.encrypt(email));
        return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
    }

    @Operation(summary = "로그인", description = "JWT 토큰을 이용한 로그인 기능")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) throws Exception {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Member user = userService.authenticate(email, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "msg", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."
            ));
        }

        String token = jwtUtil.generateToken(user);
        redisTokenRepository.saveToken(token, email, TOKEN_EXPIRATION_TIME);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "msg", "로그인 성공!",
                "userId", EncryptionUtil.decrypt(user.getUserEmail()),
                "userName", EncryptionUtil.decrypt(user.getUserName())
        ));
    }

    @Operation(summary = "현재 기기에서 로그아웃", description = "현재 사용 중인 기기에서 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        log.info("현재 기기 로그아웃 요청 처리 컨트롤러 진입");
        if (redisTokenRepository.removeToken(token)) {
            return ResponseEntity.ok("현재 기기에서 로그아웃되었습니다!");
        }
        return ResponseEntity.status(422).body("유효하지 않은 토큰입니다.");
    }

    @Operation(summary = "모든 기기에서 로그아웃", description = "사용자의 모든 기기에서 로그아웃")
    @PostMapping("/logout/all")
    public ResponseEntity<String> logoutAll(@RequestHeader("Authorization") String token) throws Exception {
        log.info("전체 기기 로그아웃 요청 처리 컨트롤러 진입");
        // JWT에서 사용자 이메일 추출
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        // 모든 기기에서 로그아웃
        int removedTokens = redisTokenRepository.removeAllTokensByEmail(email);
        if (removedTokens > 0) {
            return ResponseEntity.ok("모든 기기에서 로그아웃되었습니다!");
        }
        return ResponseEntity.status(422).body("유효하지 않은 요청입니다.");
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경하며 모든 기기에서 로그아웃")
    @PutMapping("/password/change/request")
    public ResponseEntity<String> updatePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> pwChangeRequest) throws Exception {
        log.info("비밀번호 변경 컨트롤러 진입");
        if (token == null || token.isEmpty()) return ResponseEntity.status(401).body("로그인해야 이용하실 수 있는 기능입니다");
        String email = pwChangeRequest.get("email");
        String oldPassword = pwChangeRequest.get("oldPassword");
        String newPassword = pwChangeRequest.get("newPassword");
        String confirmPassword = pwChangeRequest.get("confirmPassword");

        Member user = userService.authenticate(email, oldPassword);
        if (user == null) return ResponseEntity.status(404).body("현재 비밀번호가 틀렸습니다");

        if (!newPassword.equals(confirmPassword))
            return ResponseEntity.status(400).body("변경을 원하는 비밀번호와 비밀번호 확인이 일치하지 않습니다");

        userRepository.updateUserPwAndPwUpdatedAtByUserEmail(EncryptionUtil.encrypt(email), passwordEncoder.encode(newPassword));
        int removedTokens = redisTokenRepository.removeAllTokensByEmail(EncryptionUtil.encrypt(email));
        if (removedTokens > 0)
            return ResponseEntity.status(200).body("비밀번호가 성공적으로 변경되어 모든 기기에서 로그아웃 되었습니다.\n새로운 비밀번호를 사용하여 로그인 해주세요!");
        return ResponseEntity.status(422).body("비밀번호는 변경이 잘 되었는데요.. 모든 기기에서 로그아웃은 왠지 모르게 실패했으니 알아서 하세요 ㅇㅋ?");
    }

}
