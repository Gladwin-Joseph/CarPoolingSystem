package com.car_pooling_system.rideservice.controller;

import com.car_pooling_system.rideservice.dto.*;
import com.car_pooling_system.rideservice.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    // ── Ride Endpoints ──────────────────────────────────────

    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @PutMapping("/{rideId}")
    public ResponseEntity<RideResponse> updateRide(@PathVariable Long rideId,
                                                   @Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.ok(rideService.updateRide(rideId, request));
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.cancelRide(rideId));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.getRideById(rideId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getHostedRides(@PathVariable Long driverId) {
        return ResponseEntity.ok(rideService.getHostedRides(driverId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RideResponse>> searchRides(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(rideService.searchRides(source, destination, date));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RideResponse>> getAllAvailableRides() {
        return ResponseEntity.ok(rideService.getAllAvailableRides());
    }

    // ── Booking Endpoints ───────────────────────────────────

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> bookRide(@Valid @RequestBody BookRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.bookRide(request));
    }

    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(rideService.cancelBooking(bookingId));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(rideService.getBookingById(bookingId));
    }

    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.getBookingHistory(userId));
    }

    // ── Rating Endpoints ────────────────────────────────────

    @PostMapping("/ratings")
    public ResponseEntity<RatingResponse> rateUser(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.rateUser(request));
    }

    @GetMapping("/ratings/user/{userId}")
    public ResponseEntity<List<RatingResponse>> getRatingsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.getRatingsForUser(userId));
    }

    @GetMapping("/ratings/user/{userId}/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "averageRating", rideService.getAverageRating(userId)
        ));
    }
}
