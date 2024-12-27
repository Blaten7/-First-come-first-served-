package com.sparta.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;

@RequiredArgsConstructor
@Entity
@Data
@Table(name = "`member`")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;
    private String userEmail;
    private String userPw;
    private String userAddress;
    private String userPH;
    private String profileImg;
    private String description;
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime pwUpdatedAt;

    public Member(String email, String s, ArrayList<Object> objects) {
        this.userEmail = email;
        this.userName = s;
        this.userPw = s;
        this.userAddress = s;
        this.userPH = s;

    }

//    @OneToMany(mappedBy = "user")
//    private List<Order> orders;
//
//    @OneToMany(mappedBy = "user")
//    private List<Wishlist> wishlists;
}
