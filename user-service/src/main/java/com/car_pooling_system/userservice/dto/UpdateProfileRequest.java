package com.car_pooling_system.userservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String name;
    private String phoneNumber;

    // Driver fields
    private String licenseNumber;
    private String licensePlate;
    private String carModel;
    private Integer capacity;

    // Passenger fields
    private String govIdNumber;
    private String preferredPaymentMethod;
}
