package com.sparta.userservice.repository;

import com.sparta.userservice.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Member, Long> {

    boolean existsByUserEmail(String email);

    Optional<Member> findByUserEmail(String email);

    @Transactional
    @Modifying
    @Query("update Member " +
            "set status = 'VERIFIED' " +
            "where userEmail = :email")
    void updateStatusFindByEmail(String email);

//    @Query("select userPw from User " +
//            "where userEmail = :encryptMail")
//    String findByUserEmailToUserPw(String encryptMail);

//    boolean existsByUserPw(String oldPw);

    @Transactional
    @Modifying
    @Query("UPDATE Member U " +
            "SET U.userPw = :newPassword," +
            "U.pwUpdatedAt = CURRENT_TIMESTAMP " +
            "WHERE U.userEmail = :email")
    void updateUserPwAndPwUpdatedAtByUserEmail(String email, String newPassword);

    void deleteByUserEmail(String encrypt);
}
