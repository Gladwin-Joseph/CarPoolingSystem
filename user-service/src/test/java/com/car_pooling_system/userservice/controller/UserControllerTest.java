package com.car_pooling_system.userservice.controller;

import com.car_pooling_system.userservice.dto.*;
import com.car_pooling_system.userservice.model.Role;
import com.car_pooling_system.userservice.model.User;
import com.car_pooling_system.userservice.security.JwtUtil;
import com.car_pooling_system.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.car_pooling_system.userservice.model.User;
import com.car_pooling_system.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserService userService;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User testUser = User.builder()
                .name("Jane Doe")
                .username("janedoe")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PASSENGER)
                .build();
        userRepository.save(testUser);
        token = jwtUtil.generateToken(1L, "jane@example.com", "PASSENGER");
    }

    @Test
    @DisplayName("POST /register - should return 201 when passenger registered")
    void register_passenger_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe").username("janedoe")
                .email("jane@example.com").password("password123")
                .role(Role.PASSENGER).build();

        User mockUser = User.builder().id(1L)
                .email("jane@example.com").role(Role.PASSENGER).build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @DisplayName("POST /register - should return 400 when required fields missing")
    void register_returns400_whenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login - should return 200 with JWT token")
    void login_returns200_withToken() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        when(userService.login("jane@example.com", "password123")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("POST /login - should return 400 when credentials invalid")
    void login_returns400_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "wrongpassword");
        when(userService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{id} - should return 200 with user data")
    void getUserById_returns200() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).name("Jane Doe")
                .email("jane@example.com").role(Role.PASSENGER).build();

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @DisplayName("GET /{id} - should return 400 when user not found")
    void getUserById_returns400_whenNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new RuntimeException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /{id} - should return 200 after update")
    void updateProfile_returns200() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Jane Updated").phoneNumber("0861111111").build();

        UserResponse response = UserResponse.builder()
                .id(1L).name("Jane Updated")
                .email("jane@example.com").role(Role.PASSENGER).build();

        when(userService.updateProfile(eq(1L), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"));
    }

    @Test
    @DisplayName("GET /verify/{userId} - should return verified=true")
    void verifyUser_returnsVerifiedTrue() throws Exception {
        when(userService.verifyUser(1L, "PASSENGER")).thenReturn(true);

        mockMvc.perform(get("/api/users/verify/1")
                        .param("userType", "PASSENGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("GET /verify/{userId} - should return verified=false")
    void verifyUser_returnsVerifiedFalse() throws Exception {
        when(userService.verifyUser(1L, "DRIVER")).thenReturn(false);

        mockMvc.perform(get("/api/users/verify/1")
                        .param("userType", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }
}