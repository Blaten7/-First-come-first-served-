package com.sparta.webapi.controller;

import com.sparta.domain.dto.UserSignupRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Operation(summary = "회원가입 - 이메일 인증", description = "사용자가 이메일을 통해 회원가입을 진행합니다.")
    @ApiResponse(responseCode = "202", description = "인증메일 전송 성공")
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody UserSignupRequestDto userRequest) {

        return ResponseEntity
                .status(202)
                .body(Map.of("msg",
                        String.format(
                                "%s 으로 인증메일이 전송되었습니다. 메일을 확인해주세요.",
                                userRequest.getUserEmail())));
    }

    @Operation(summary = "이메일 인증 확인", description = "이메일 인증을 확인하는 엔드포인트입니다.")
    @GetMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestParam String token) {
        return Map.of("msg", "이메일 인증이 완료되었습니다!");
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
