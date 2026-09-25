package com.portfolio.commerce.security;
import com.portfolio.commerce.domain.user.User;
import jakarta.persistence.*;
@Entity @Table(name="auth_accounts")
public class AuthAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false,unique=true) private User user;
 @Column(nullable=false,unique=true,length=255) private String email;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Role role;
 protected AuthAccount() {}
 public AuthAccount(User user,String email,String passwordHash,Role role){this.user=user;this.email=email;this.passwordHash=passwordHash;this.role=role;}
 public Long getId(){return id;} public User getUser(){return user;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;}
}
