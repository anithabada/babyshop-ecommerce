package com.babyshop.service;

import com.babyshop.dao.UserDao;
import com.babyshop.dto.UserResponse;
import com.babyshop.exception.ValidationException;
import com.babyshop.model.User;

import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private final UserDao userDao = new UserDao();
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-() ]{7,20}$");

    public UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getCreatedAt());
    }

    public UserResponse updateProfile(User user, String name, String phone) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("name", name, "Name is required");
        v.check("name", name == null || (name.length() >= 2 && name.length() <= 100), "Name must be between 2 and 100 characters");
        v.require("phone", phone, "Phone number is required");
        v.check("phone", phone == null || PHONE_PATTERN.matcher(phone).matches(), "Phone number is invalid");
        v.throwIfInvalid();

        user.setName(name.trim());
        user.setPhone(phone.trim());
        userDao.update(user);
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userDao.findAll().stream().map(this::toResponse).toList();
    }
}
