package com.sparta.userservice.repository;

import com.sparta.userservice.entity.Member;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<Member, Long> {

    Mono<Boolean> existsByUserEmail(String email);

    @Query("select * from member where user_email = :email and status = 'VERIFIED'")
    Mono<Member> findByUserEmail(String email);

    @Query("UPDATE member SET status = 'VERIFIED' WHERE user_email = :email")
    Mono<Void> updateStatusFindByEmail(String email);

    @Query("UPDATE member SET userPw = :newPassword, pwUpdatedAt = NOW() WHERE userEmail = :email")
    Mono<Void> updateUserPwAndPwUpdatedAtByUserEmail(String email, String newPassword);

    @Query("DELETE FROM member WHERE userEmail = :email")
    Mono<Void> deleteByUserEmail(String email);
}

