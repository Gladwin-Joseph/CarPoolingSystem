package com.car_pooling_system.rideservice.service;

import com.car_pooling_system.rideservice.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface RideService {

    // Ride CRUD
    RideResponse createRide(CreateRideRequest request);
    RideResponse updateRide(Long rideId, CreateRideRequest request);
    RideResponse cancelRide(Long rideId);
    RideResponse getRideById(Long rideId);
    List<RideResponse> getHostedRides(Long driverId);
    List<RideResponse> searchRides(String source, String destination, LocalDateTime date);
    List<RideResponse> getAllAvailableRides();

    // Booking
    BookingResponse bookRide(BookRideRequest request);
    BookingResponse cancelBooking(Long bookingId);
    BookingResponse getBookingById(Long bookingId);
    List<BookingResponse> getBookingHistory(Long userId);

    // Rating
    RatingResponse rateUser(RatingRequest request);
    List<RatingResponse> getRatingsForUser(Long userId);
    Double getAverageRating(Long userId);
}
