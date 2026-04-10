package com.car_pooling_system.rideservice.repository;

import com.car_pooling_system.rideservice.model.Booking;
import com.car_pooling_system.rideservice.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByRideId(Long rideId);
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
}
