package com.car_pooling_system.userservice.dto;

import com.car_pooling_system.userservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Role is required (DRIVER or PASSENGER)")
    private Role role;

    // Driver-specific fields (optional, only if role = DRIVER)
    private String licenseNumber;
    private String licensePlate;
    private String carModel;
    private Integer capacity;

    // Passenger-specific fields (optional, only if role = PASSENGER)
    private String govIdNumber;
    private String preferredPaymentMethod;
}
