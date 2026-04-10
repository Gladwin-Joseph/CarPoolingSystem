package com.car_pooling_system.userservice.service;

import com.car_pooling_system.userservice.dto.RegisterRequest;
import com.car_pooling_system.userservice.dto.UpdateProfileRequest;
import com.car_pooling_system.userservice.dto.UserResponse;
import com.car_pooling_system.userservice.model.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(String email, String password);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    boolean verifyUser(Long userId, String userType);
}
