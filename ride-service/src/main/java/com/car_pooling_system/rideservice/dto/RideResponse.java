package com.car_pooling_system.rideservice.dto;

import com.car_pooling_system.rideservice.model.RideStatus;

import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponse {
    private Long id;
    private Long driverId;
    private String source;
    private String destination;
    private LocalDateTime departureDatetime;
    private LocalDateTime arrivalDatetime;
    private BigDecimal price;
    private int availableSeats;
    private RideStatus status;
    private LocalDateTime createdAt;
}
