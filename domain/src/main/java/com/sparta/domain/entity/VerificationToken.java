package com.sparta.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "verificationToken")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // 고유 토큰 값
    @Column(nullable = false)
    private String userEmail;
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate; // 만료 시각

    public VerificationToken(String token, String userEmail, LocalDateTime expiryDate) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = expiryDate;
    }

}
