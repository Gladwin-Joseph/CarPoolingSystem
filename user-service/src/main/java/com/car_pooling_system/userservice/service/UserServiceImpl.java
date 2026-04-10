package com.car_pooling_system.userservice.service;

import com.car_pooling_system.userservice.dto.RegisterRequest;
import com.car_pooling_system.userservice.dto.UpdateProfileRequest;
import com.car_pooling_system.userservice.dto.UserResponse;
import com.car_pooling_system.userservice.factory.RoleBasedUserFactory;
import com.car_pooling_system.userservice.model.*;
import com.car_pooling_system.userservice.repository.*;
import com.car_pooling_system.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final HostDriverRepository hostDriverRepository;
    private final PassengerRepository passengerRepository;
    private final VehicleRepository vehicleRepository;
    private final RoleBasedUserFactory userFactory; // Singleton Factory injected via Spring
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // Validate uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Build base User
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        // Factory Method Pattern: create role-specific entity
        if (request.getRole() == Role.DRIVER) {
            HostDriver driver = userFactory.createDriver(user);
            if (request.getLicenseNumber() != null) {
                driver.setLicenseNumber(request.getLicenseNumber());
            }
            driver = hostDriverRepository.save(driver);

            // Create Vehicle if details provided
            if (request.getLicensePlate() != null) {
                Vehicle vehicle = Vehicle.builder()
                        .driver(driver)
                        .licensePlate(request.getLicensePlate())
                        .carModel(request.getCarModel() != null ? request.getCarModel() : "NOT_SET")
                        .capacity(request.getCapacity() != null ? request.getCapacity() : 4)
                        .build();
                vehicleRepository.save(vehicle);
            }
        } else if (request.getRole() == Role.PASSENGER) {
            Passenger passenger = userFactory.createPassenger(user);
            if (request.getGovIdNumber() != null) {
                passenger.setGovIdNumber(request.getGovIdNumber());
            }
            if (request.getPreferredPaymentMethod() != null) {
                passenger.setPreferredPaymentMethod(request.getPreferredPaymentMethod());
            }
            passengerRepository.save(passenger);
        }

        return user;
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return buildUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return buildUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(user);

        // Update role-specific fields
        if (user.getRole() == Role.DRIVER) {
            HostDriver driver = hostDriverRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Driver profile not found"));
            if (request.getLicenseNumber() != null) driver.setLicenseNumber(request.getLicenseNumber());
            hostDriverRepository.save(driver);

            // Update vehicle
            if (request.getLicensePlate() != null || request.getCarModel() != null || request.getCapacity() != null) {
                Vehicle vehicle = vehicleRepository.findByDriverId(driver.getId())
                        .orElse(Vehicle.builder().driver(driver).licensePlate("NOT_SET").carModel("NOT_SET").capacity(4).build());
                if (request.getLicensePlate() != null) vehicle.setLicensePlate(request.getLicensePlate());
                if (request.getCarModel() != null) vehicle.setCarModel(request.getCarModel());
                if (request.getCapacity() != null) vehicle.setCapacity(request.getCapacity());
                vehicleRepository.save(vehicle);
            }
        } else if (user.getRole() == Role.PASSENGER) {
            Passenger passenger = passengerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Passenger profile not found"));
            if (request.getGovIdNumber() != null) passenger.setGovIdNumber(request.getGovIdNumber());
            if (request.getPreferredPaymentMethod() != null) passenger.setPreferredPaymentMethod(request.getPreferredPaymentMethod());
            passengerRepository.save(passenger);
        }

        return buildUserResponse(user);
    }

    @Override
    public boolean verifyUser(Long userId, String userType) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return user.getRole().name().equalsIgnoreCase(userType);
    }

    // ── Helper ──────────────────────────────────────────────

    private UserResponse buildUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole());

        if (user.getRole() == Role.DRIVER) {
            hostDriverRepository.findByUserId(user.getId()).ifPresent(driver -> {
                builder.licenseNumber(driver.getLicenseNumber());
                vehicleRepository.findByDriverId(driver.getId()).ifPresent(vehicle -> {
                    builder.licensePlate(vehicle.getLicensePlate());
                    builder.carModel(vehicle.getCarModel());
                    builder.capacity(vehicle.getCapacity());
                });
            });
        } else if (user.getRole() == Role.PASSENGER) {
            passengerRepository.findByUserId(user.getId()).ifPresent(passenger -> {
                builder.govIdNumber(passenger.getGovIdNumber());
                builder.preferredPaymentMethod(passenger.getPreferredPaymentMethod());
            });
        }

        return builder.build();
    }
}
