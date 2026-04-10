package com.car_pooling_system.rideservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    private Long id;
    private Long rideId;
    private Long ratedByUserId;
    private Long ratedUserId;
    private int rating;
    private String userType;
    private String comment;
    private LocalDateTime createdAt;
}
