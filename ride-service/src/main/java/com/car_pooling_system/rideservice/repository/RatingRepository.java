package com.car_pooling_system.rideservice.repository;

import com.car_pooling_system.rideservice.model.BookingStatus;
import com.car_pooling_system.rideservice.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRideId(Long rideId);
    List<Rating> findByRatedUserId(Long ratedUserId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUserId = :userId")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
}
