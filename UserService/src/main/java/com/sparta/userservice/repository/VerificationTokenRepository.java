package com.sparta.userservice.repository;

import com.sparta.userservice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    // 토큰 값으로 검증 데이터를 조회
    boolean findByToken(String token);

    @Query(value = "SELECT COUNT(*) " +
            "FROM verification_token vt " +
            "WHERE vt.token = :token AND vt.expiry_date > CURRENT_TIMESTAMP", nativeQuery = true)
    Long countByTokenAndExpiryDateAfter(@Param("token") String token);

    @Query(value = "SELECT vt.expiry_date FROM verification_token vt WHERE vt.token = :token", nativeQuery = true)
    Object getExpiryDate(@Param("token") String token);

}
