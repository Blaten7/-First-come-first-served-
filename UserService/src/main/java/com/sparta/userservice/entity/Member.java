package com.sparta.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Table(name = "member")
public class Member {

    @Id
    @Column("user_id")
    private Long userId;
    @Column("user_name")
    private String userName;
    @Column("user_email")
    private String userEmail;
    @Column("user_pw")
    private String userPw;
    @Column("user_address")
    private String userAddress;
    @Column("user_ph")
    private String userPH;
    @Column("profile_img")
    private String profileImg;
    @Column("description")
    private String description;
    @Column("status")
    private String status; // 사용자의 상태 (e.g., ACTIVE, INACTIVE)

    // 권한 필드: 여러 권한을 가진 경우 구분자로 저장 (예: "ROLE_USER,ROLE_ADMIN")
    private String roles;

    // 생성 및 업데이트 시간
    private LocalDateTime createdAt;
    private LocalDateTime pwUpdatedAt;

    // 기본 생성자
    public Member() {
        this.createdAt = LocalDateTime.now();
        this.pwUpdatedAt = LocalDateTime.now();
    }

    public Member(String email, String defaultValue, List<Object> roles) {
        this.userEmail = email;
        this.userName = defaultValue;
        this.userPw = defaultValue;
        this.roles = String.join(",", roles.stream().map(Object::toString).toList()); // 권한 리스트를 String으로 변환
        this.createdAt = LocalDateTime.now();
        this.pwUpdatedAt = LocalDateTime.now();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.roles));
        return authorities;
    }
}
