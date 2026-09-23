package com.babyshop.service;

import com.babyshop.dao.CartDao;
import com.babyshop.dao.UserDao;
import com.babyshop.dto.AuthResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.exception.ValidationException;
import com.babyshop.model.Role;
import com.babyshop.model.User;
import com.babyshop.util.JwtUtil;
import com.babyshop.util.PasswordUtil;

import java.util.regex.Pattern;

public class AuthService {

    private final UserDao userDao = new UserDao();
    private final CartDao cartDao = new CartDao();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-() ]{7,20}$");

    public AuthResponse register(String name, String email, String phone, String password) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("name", name, "Name is required");
        v.check("name", name == null || (name.length() >= 2 && name.length() <= 100), "Name must be between 2 and 100 characters");
        v.require("email", email, "Email is required");
        v.check("email", email == null || EMAIL_PATTERN.matcher(email).matches(), "Email must be a valid email address");
        v.require("phone", phone, "Phone number is required");
        v.check("phone", phone == null || PHONE_PATTERN.matcher(phone).matches(), "Phone number is invalid");
        v.require("password", password, "Password is required");
        v.check("password", password == null || password.length() >= 6, "Password must be at least 6 characters long");
        v.throwIfInvalid();

        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.existsByEmail(normalizedEmail)) {
            throw ApiException.conflict("An account with this email already exists");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPhone(phone.trim());
        user.setPassword(PasswordUtil.hash(password));
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);

        User saved = userDao.save(user);

        // Every new customer gets an empty cart ready to use
        cartDao.findOrCreateByUserId(saved.getId());

        String token = JwtUtil.generateToken(saved.getEmail());
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }

    public AuthResponse login(String email, String password) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("email", email, "Email is required");
        v.require("password", password, "Password is required");
        v.throwIfInvalid();

        User user = userDao.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw ApiException.forbidden("This account has been disabled");
        }

        String token = JwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
