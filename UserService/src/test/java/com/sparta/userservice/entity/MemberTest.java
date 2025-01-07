package com.sparta.userservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        // When
        Member member = new Member();

        // Then
        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("이메일과 사용자명으로 생성자 테스트")
    void testEmailUsernameConstructor() {
        // Given
        String email = "test@example.com";
        String username = "testUser";

        // When
        Member member = new Member(email, username);

        // Then
        assertThat(member.getUserEmail()).isEqualTo(email);
        assertThat(member.getUserName()).isEqualTo(username);
    }

    @Test
    @DisplayName("전체 필드 생성자 테스트")
    void testFullConstructor() {
        // Given
        String email = "test@example.com";
        String defaultValue = "test";
        ArrayList<Object> objects = new ArrayList<>();

        // When
        Member member = new Member(email, defaultValue, objects);

        // Then
        assertThat(member.getUserEmail()).isEqualTo(email);
        assertThat(member.getUserName()).isEqualTo(defaultValue);
        assertThat(member.getUserPw()).isEqualTo(defaultValue);
        assertThat(member.getUserAddress()).isEqualTo(defaultValue);
        assertThat(member.getUserPH()).isEqualTo(defaultValue);
    }

    @Test
    @DisplayName("필드 값 설정/조회 테스트")
    void testSetterGetter() {
        // Given
        Member member = new Member();
        LocalDateTime now = LocalDateTime.now();

        // When
        member.setUserId(1L);
        member.setUserName("testUser");
        member.setUserEmail("test@example.com");
        member.setUserPw("password123");
        member.setUserAddress("서울시 강남구");
        member.setUserPH("010-1234-5678");
        member.setProfileImg("profile.jpg");
        member.setDescription("테스트 설명");
        member.setStatus("ACTIVE");
        member.setCreatedAt(now);
        member.setPwUpdatedAt(now);

        // Then
        assertThat(member.getUserId()).isEqualTo(1L);
        assertThat(member.getUserName()).isEqualTo("testUser");
        assertThat(member.getUserEmail()).isEqualTo("test@example.com");
        assertThat(member.getUserPw()).isEqualTo("password123");
        assertThat(member.getUserAddress()).isEqualTo("서울시 강남구");
        assertThat(member.getUserPH()).isEqualTo("010-1234-5678");
        assertThat(member.getProfileImg()).isEqualTo("profile.jpg");
        assertThat(member.getDescription()).isEqualTo("테스트 설명");
        assertThat(member.getStatus()).isEqualTo("ACTIVE");
        assertThat(member.getCreatedAt()).isEqualTo(now);
        assertThat(member.getPwUpdatedAt()).isEqualTo(now);
    }
}