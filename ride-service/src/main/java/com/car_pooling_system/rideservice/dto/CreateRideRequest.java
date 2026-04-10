package com.car_pooling_system.rideservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRideRequest {

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure datetime is required")
    @Future(message = "Departure must be in the future")
    private LocalDateTime departureDatetime;

    private LocalDateTime arrivalDatetime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "At least 1 seat must be available")
    private int availableSeats;
}
