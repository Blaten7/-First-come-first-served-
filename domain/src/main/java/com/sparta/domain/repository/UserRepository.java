package com.sparta.domain.repository;

import com.sparta.domain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmailAndStatus(String email, String status);

    boolean existsByUserEmail(String email);

    Optional<User> findByUserEmail(String email);

    @Transactional
    @Modifying
    @Query("update User " +
            "set status = 'VERIFIED' " +
            "where userEmail = :email")
    void updateStatusFindByEmail(String email);

    @Query("select userPw from User " +
            "where userEmail = :encryptMail")
    String findByUserEmailToUserPw(String encryptMail);
}
