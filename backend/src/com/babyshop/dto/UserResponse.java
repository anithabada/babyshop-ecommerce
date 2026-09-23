package com.babyshop.dto;

import java.time.LocalDateTime;

public class UserResponse {
    public Long id;
    public String name;
    public String email;
    public String phone;
    public String role;
    public LocalDateTime createdAt;

    public UserResponse(Long id, String name, String email, String phone, String role, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.email = email;
        this.phone = phone; this.role = role; this.createdAt = createdAt;
    }
}
