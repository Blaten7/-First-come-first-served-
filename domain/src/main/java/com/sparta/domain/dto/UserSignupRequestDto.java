package com.sparta.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserSignupRequestDto {

    @NotBlank(message = "이름을 입력해주세요.")
    private String userName;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "유효한 이메일 주소를 입력해주세요."
    )
    private String userEmail;


    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$",
            message = "비밀번호는 최소 8자 이상이어야 하며, 숫자와 특수 문자를 포함해야 합니다."
    )
    private String userPw;

    @NotBlank(message = "주소를 입력해주세요")
    private String userAddress;

    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    @Pattern(
            regexp = "^01[01]-\\d{4}-\\d{4}$",
            message = "유효한 휴대폰 번호를 입력해주세요. (예: 010-1234-5678)"
    )
    private String userPH;

    private String profileImg;
    private String description;
}
