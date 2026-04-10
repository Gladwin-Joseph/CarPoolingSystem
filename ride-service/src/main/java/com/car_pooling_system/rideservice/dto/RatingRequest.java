package com.car_pooling_system.rideservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Rated by user ID is required")
    private Long ratedByUserId;

    @NotNull(message = "Rated user ID is required")
    private Long ratedUserId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "User type is required (DRIVER or PASSENGER)")
    private String userType;

    private String comment;
}
