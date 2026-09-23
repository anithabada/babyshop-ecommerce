package com.babyshop.model;

import java.time.LocalDateTime;

/** Internal representation of a user row - includes the password hash, so
 *  this class must NEVER be serialized directly to a JSON response. */
public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String password; // BCrypt hash
    private Role role = Role.ROLE_CUSTOMER;
    private boolean enabled = true;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
