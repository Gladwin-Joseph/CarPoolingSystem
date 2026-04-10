package com.car_pooling_system.rideservice.service;

import com.car_pooling_system.rideservice.client.UserServiceClient;
import com.car_pooling_system.rideservice.dto.*;
import com.car_pooling_system.rideservice.model.*;
import com.car_pooling_system.rideservice.observer.*;
import com.car_pooling_system.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final RatingRepository ratingRepository;
    private final UserServiceClient userServiceClient;
    private final RideEventManager rideEventManager; // Observer pattern Subject

    // ── Ride CRUD ───────────────────────────────────────────

    @Override
    @Transactional
    public RideResponse createRide(CreateRideRequest request) {
        // Verify user is a DRIVER via User Service (Feign + Circuit Breaker)
        Map<String, Object> verification = userServiceClient.verifyUser(request.getDriverId(), "DRIVER");

        // Check if this is a fallback response (User Service down)
        if (verification.containsKey("fallback") && (Boolean) verification.get("fallback")) {
            log.warn("[RIDE CREATE] User Service unavailable. Rejecting ride creation for driver {}.",
                    request.getDriverId());
            throw new RuntimeException("User Service is currently unavailable. Cannot verify driver. Please try again later.");
        }

        if (!(Boolean) verification.get("verified")) {
            throw new RuntimeException("User is not a verified driver");
        }

        Ride ride = Ride.builder()
                .driverId(request.getDriverId())
                .source(request.getSource())
                .destination(request.getDestination())
                .departureDatetime(request.getDepartureDatetime())
                .arrivalDatetime(request.getArrivalDatetime())
                .price(request.getPrice())
                .availableSeats(request.getAvailableSeats())
                .status(RideStatus.SCHEDULED)
                .build();

        ride = rideRepository.save(ride);

        // Observer: attach the driver as an observer of their own ride
        rideEventManager.attach(ride.getId(), new DriverObserver(request.getDriverId()));
        rideEventManager.notifyObservers(ride.getId(), "RIDE_CREATED",
                "Ride from " + ride.getSource() + " to " + ride.getDestination() + " created successfully");

        log.info("[RIDE CREATED] Ride #{} by driver {} from {} to {}",
                ride.getId(), ride.getDriverId(), ride.getSource(), ride.getDestination());

        return toRideResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse updateRide(Long rideId, CreateRideRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled rides can be updated");
        }

        ride.setSource(request.getSource());
        ride.setDestination(request.getDestination());
        ride.setDepartureDatetime(request.getDepartureDatetime());
        ride.setArrivalDatetime(request.getArrivalDatetime());
        ride.setPrice(request.getPrice());
        ride.setAvailableSeats(request.getAvailableSeats());

        ride = rideRepository.save(ride);

        // Observer: notify all observers of ride update
        rideEventManager.notifyObservers(ride.getId(), "RIDE_UPDATED",
                "Ride details have been updated");

        return toRideResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse cancelRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new RuntimeException("Ride is already cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride = rideRepository.save(ride);

        // Cancel all active bookings for this ride
        List<Booking> activeBookings = bookingRepository.findByRideId(rideId).stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();
        for (Booking booking : activeBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }

        // Observer: notify all observers of cancellation
        rideEventManager.notifyObservers(ride.getId(), "RIDE_CANCELLED",
                "Ride has been cancelled. All bookings refunded.");

        return toRideResponse(ride);
    }

    @Override
    public RideResponse getRideById(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        return toRideResponse(ride);
    }

    @Override
    public List<RideResponse> getHostedRides(Long driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(this::toRideResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponse> searchRides(String source, String destination, LocalDateTime date) {
        return rideRepository.searchRides(source, destination, date).stream()
                .map(this::toRideResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponse> getAllAvailableRides() {
        return rideRepository.findAllAvailableRides().stream()
                .map(this::toRideResponse)
                .collect(Collectors.toList());
    }

    // ── Booking ─────────────────────────────────────────────

    @Override
    @Transactional
    public BookingResponse bookRide(BookRideRequest request) {
        // Verify user is a PASSENGER via User Service (Feign + Circuit Breaker)
        Map<String, Object> verification = userServiceClient.verifyUser(request.getUserId(), "PASSENGER");

        // Check if this is a fallback response (User Service down)
        if (verification.containsKey("fallback") && (Boolean) verification.get("fallback")) {
            log.warn("[BOOKING] User Service unavailable. Rejecting booking for user {}.",
                    request.getUserId());
            throw new RuntimeException("User Service is currently unavailable. Cannot verify passenger. Please try again later.");
        }

        if (!(Boolean) verification.get("verified")) {
            throw new RuntimeException("User is not a verified passenger");
        }

        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new RuntimeException("Ride is not available for booking");
        }

        if (ride.getAvailableSeats() < request.getBookedSeats()) {
            throw new RuntimeException("Not enough seats available. Remaining: " + ride.getAvailableSeats());
        }

        // Calculate amount
        BigDecimal amount = ride.getPrice().multiply(BigDecimal.valueOf(request.getBookedSeats()));

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .ride(ride)
                .status(BookingStatus.CONFIRMED)
                .bookedSeats(request.getBookedSeats())
                .amount(amount)
                .build();

        booking = bookingRepository.save(booking);

        // Update available seats
        ride.setAvailableSeats(ride.getAvailableSeats() - request.getBookedSeats());
        rideRepository.save(ride);

        // Observer: attach passenger as observer and notify
        rideEventManager.attach(ride.getId(), new PassengerObserver(request.getUserId()));
        rideEventManager.notifyObservers(ride.getId(), "BOOKING_CONFIRMED",
                "Passenger " + request.getUserId() + " booked " + request.getBookedSeats() + " seat(s)");

        log.info("[BOOKING CONFIRMED] Booking #{} | User {} | Ride #{} | Seats: {} | Amount: {}",
                booking.getId(), booking.getUserId(), ride.getId(),
                booking.getBookedSeats(), booking.getAmount());

        return toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Restore seats
        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getBookedSeats());
        rideRepository.save(ride);

        // Observer: notify cancellation
        rideEventManager.notifyObservers(ride.getId(), "BOOKING_CANCELLED",
                "Booking #" + bookingId + " cancelled. " + booking.getBookedSeats() + " seat(s) restored.");

        return toBookingResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return toBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingHistory(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    // ── Rating ──────────────────────────────────────────────

    @Override
    @Transactional
    public RatingResponse rateUser(RatingRequest request) {
        Rating rating = Rating.builder()
                .rideId(request.getRideId())
                .ratedByUserId(request.getRatedByUserId())
                .ratedUserId(request.getRatedUserId())
                .minValue(1)
                .maxValue(5)
                .userType(RatingUserType.valueOf(request.getUserType().toUpperCase()))
                .comment(request.getComment())
                .build();

        rating.rate(request.getRating()); // validates value within range

        rating = ratingRepository.save(rating);
        return toRatingResponse(rating);
    }

    @Override
    public List<RatingResponse> getRatingsForUser(Long userId) {
        return ratingRepository.findByRatedUserId(userId).stream()
                .map(this::toRatingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRating(Long userId) {
        Double avg = ratingRepository.findAverageRatingByUserId(userId);
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    // ── Mappers ─────────────────────────────────────────────

    private RideResponse toRideResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .driverId(ride.getDriverId())
                .source(ride.getSource())
                .destination(ride.getDestination())
                .departureDatetime(ride.getDepartureDatetime())
                .arrivalDatetime(ride.getArrivalDatetime())
                .price(ride.getPrice())
                .availableSeats(ride.getAvailableSeats())
                .status(ride.getStatus())
                .createdAt(ride.getCreatedAt())
                .build();
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .rideId(booking.getRide().getId())
                .status(booking.getStatus())
                .bookedSeats(booking.getBookedSeats())
                .amount(booking.getAmount())
                .bookingDatetime(booking.getBookingDatetime())
                .build();
    }

    private RatingResponse toRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .rideId(rating.getRideId())
                .ratedByUserId(rating.getRatedByUserId())
                .ratedUserId(rating.getRatedUserId())
                .rating(rating.getRating())
                .userType(rating.getUserType().name())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
