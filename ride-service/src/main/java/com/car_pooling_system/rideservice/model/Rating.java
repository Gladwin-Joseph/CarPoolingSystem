package com.car_pooling_system.rideservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "rated_by_user_id", nullable = false)
    private Long ratedByUserId;

    @Column(name = "rated_user_id", nullable = false)
    private Long ratedUserId;

    @Column(nullable = false)
    private int rating;

    @Column(name = "min_value")
    @Builder.Default
    private int minValue = 1;

    @Column(name = "max_value")
    @Builder.Default
    private int maxValue = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private RatingUserType userType;

    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void rate(int value) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException("Rating must be between " + minValue + " and " + maxValue);
        }
        this.rating = value;
    }
}
