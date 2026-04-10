package com.car_pooling_system.userservice.dto;

import com.car_pooling_system.userservice.model.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;

    // Driver fields (null if passenger)
    private String licenseNumber;
    private String licensePlate;
    private String carModel;
    private Integer capacity;

    // Passenger fields (null if driver)
    private String govIdNumber;
    private String preferredPaymentMethod;
}
