package com.car_pooling_system.userservice.service;

import com.car_pooling_system.userservice.dto.RegisterRequest;
import com.car_pooling_system.userservice.dto.UpdateProfileRequest;
import com.car_pooling_system.userservice.dto.UserResponse;
import com.car_pooling_system.userservice.factory.RoleBasedUserFactory;
import com.car_pooling_system.userservice.model.*;
import com.car_pooling_system.userservice.repository.*;
import com.car_pooling_system.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private HostDriverRepository hostDriverRepository;
    @Mock private PassengerRepository passengerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private RoleBasedUserFactory userFactory;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User passengerUser;
    private User driverUser;

    @BeforeEach
    void setUp() {
        passengerUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .username("janedoe")
                .email("jane@example.com")
                .password("encodedPassword")
                .phoneNumber("0851234567")
                .role(Role.PASSENGER)
                .build();

        driverUser = User.builder()
                .id(2L)
                .name("John Driver")
                .username("johndriver")
                .email("john@example.com")
                .password("encodedPassword")
                .phoneNumber("0859876543")
                .role(Role.DRIVER)
                .build();
    }

    // ── register() ──────────────────────────────────────────

    @Test
    @DisplayName("register() - should register a PASSENGER successfully")
    void register_passenger_success() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .username("janedoe")
                .email("jane@example.com")
                .password("password123")
                .phoneNumber("0851234567")
                .role(Role.PASSENGER)
                .govIdNumber("GOV-123")
                .preferredPaymentMethod("CARD")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(passengerUser);

        Passenger mockPassenger = new Passenger();
        when(userFactory.createPassenger(any(User.class))).thenReturn(mockPassenger);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(mockPassenger);

        User result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getRole()).isEqualTo(Role.PASSENGER);
        verify(passengerRepository).save(any(Passenger.class));
        verify(hostDriverRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - should register a DRIVER with vehicle successfully")
    void register_driver_with_vehicle_success() {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Driver")
                .username("johndriver")
                .email("john@example.com")
                .password("password123")
                .role(Role.DRIVER)
                .licenseNumber("LIC-456")
                .licensePlate("191-LK-123")
                .carModel("Toyota Corolla")
                .capacity(4)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(driverUser);

        HostDriver mockDriver = new HostDriver();
        when(userFactory.createDriver(any(User.class))).thenReturn(mockDriver);
        when(hostDriverRepository.save(any(HostDriver.class))).thenReturn(mockDriver);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(new Vehicle());

        User result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.DRIVER);
        verify(hostDriverRepository).save(any(HostDriver.class));
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("register() - should throw exception when email already exists")
    void register_throwsException_whenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("jane@example.com")
                .username("janedoe")
                .role(Role.PASSENGER)
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - should throw exception when username already taken")
    void register_throwsException_whenUsernameExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .username("janedoe")
                .role(Role.PASSENGER)
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already taken");
    }

    // ── login() ─────────────────────────────────────────────

    @Test
    @DisplayName("login() - should return JWT token on valid credentials")
    void login_success() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(passengerUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "jane@example.com", "PASSENGER")).thenReturn("mock-jwt-token");

        String token = userService.login("jane@example.com", "password123");

        assertThat(token).isEqualTo("mock-jwt-token");
    }

    @Test
    @DisplayName("login() - should throw exception when email not found")
    void login_throwsException_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("unknown@example.com", "password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login() - should throw exception when password does not match")
    void login_throwsException_whenPasswordWrong() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(passengerUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("jane@example.com", "wrongPassword"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ── getUserById() ────────────────────────────────────────

    @Test
    @DisplayName("getUserById() - should return user response when user exists")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(passengerUser));
        when(passengerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getRole()).isEqualTo(Role.PASSENGER);
    }

    @Test
    @DisplayName("getUserById() - should throw exception when user not found")
    void getUserById_throwsException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ── updateProfile() ──────────────────────────────────────

    @Test
    @DisplayName("updateProfile() - should update name and phone for passenger")
    void updateProfile_passenger_success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Jane Updated")
                .phoneNumber("0861111111")
                .build();

        Passenger passenger = new Passenger();
        when(userRepository.findById(1L)).thenReturn(Optional.of(passengerUser));
        when(userRepository.save(any(User.class))).thenReturn(passengerUser);
        when(passengerRepository.findByUserId(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);

        UserResponse result = userService.updateProfile(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile() - should throw exception when user not found")
    void updateProfile_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(99L, new UpdateProfileRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── verifyUser() ─────────────────────────────────────────

    @Test
    @DisplayName("verifyUser() - should return true when user role matches")
    void verifyUser_returnsTrue_whenRoleMatches() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(passengerUser));

        boolean result = userService.verifyUser(1L, "PASSENGER");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyUser() - should return false when user role does not match")
    void verifyUser_returnsFalse_whenRoleMismatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(passengerUser));

        boolean result = userService.verifyUser(1L, "DRIVER");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyUser() - should return false when user not found")
    void verifyUser_returnsFalse_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = userService.verifyUser(99L, "PASSENGER");

        assertThat(result).isFalse();
    }
}