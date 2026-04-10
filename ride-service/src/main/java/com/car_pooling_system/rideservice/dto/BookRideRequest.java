package com.car_pooling_system.rideservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRideRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @Min(value = 1, message = "At least 1 seat must be booked")
    private int bookedSeats;
}
