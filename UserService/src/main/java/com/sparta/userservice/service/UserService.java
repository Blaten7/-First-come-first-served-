package com.sparta.userservice.service;

import com.sparta.userservice.dto.UserSignupRequestDto;
import com.sparta.userservice.entity.Member;

public interface UserService {
    String createVerificationToken(UserSignupRequestDto userRequest);

    void saveTempUser(UserSignupRequestDto userRequest) throws Exception;

    Member authenticate(String email, String password) throws Exception;

    String generateToken();

    void sendEmail(String toEmail, String subject, String verificationLink);
}
