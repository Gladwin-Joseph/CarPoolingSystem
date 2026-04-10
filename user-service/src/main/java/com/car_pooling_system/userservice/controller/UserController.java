package com.car_pooling_system.userservice.controller;

import com.car_pooling_system.userservice.dto.*;
import com.car_pooling_system.userservice.model.User;
import com.car_pooling_system.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ── Auth Endpoints ──────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "status", "success",
                        "message", "User registered successfully as " + req.getRole(),
                        "userId", user.getId(),
                        "email", user.getEmail()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = userService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ── Profile Endpoints ───────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
                                                      @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }

    // ── Verification Endpoint (called by other services via Feign) ──

    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyUser(@PathVariable Long userId,
                                                          @RequestParam String userType) {
        boolean verified = userService.verifyUser(userId, userType);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "userType", userType,
                "verified", verified
        ));
    }
}
