package com.sparta.domain.repository;

import com.sparta.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    // 토큰 값으로 검증 데이터를 조회
    boolean findByToken(String token);

    // 토큰 값과 만료 여부를 함께 검증
    Optional<VerificationToken> findByTokenAndExpiredFalse(String token);

    boolean findByTokenAndExpiryDateAfter(String token, LocalDateTime now);
}
