package com.car_pooling_system.rideservice.service;

import com.car_pooling_system.rideservice.client.UserServiceClient;
import com.car_pooling_system.rideservice.dto.*;
import com.car_pooling_system.rideservice.model.*;
import com.car_pooling_system.rideservice.observer.RideEventManager;
import com.car_pooling_system.rideservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideServiceImpl Unit Tests")
class RideServiceImplTest {

    @Mock private RideRepository rideRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private RatingRepository ratingRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private RideEventManager rideEventManager;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride scheduledRide;
    private CreateRideRequest createRideRequest;

    @BeforeEach
    void setUp() {
        scheduledRide = Ride.builder()
                .id(1L)
                .driverId(2L)
                .source("Limerick")
                .destination("Dublin")
                .departureDatetime(LocalDateTime.now().plusDays(1))
                .price(new BigDecimal("15.00"))
                .availableSeats(3)
                .status(RideStatus.SCHEDULED)
                .build();

        createRideRequest = CreateRideRequest.builder()
                .driverId(2L)
                .source("Limerick")
                .destination("Dublin")
                .departureDatetime(LocalDateTime.now().plusDays(1))
                .price(new BigDecimal("15.00"))
                .availableSeats(3)
                .build();
    }

    // ── createRide() ─────────────────────────────────────────

    @Test
    @DisplayName("createRide() - should create ride when driver is verified")
    void createRide_success() {
        when(userServiceClient.verifyUser(2L, "DRIVER"))
                .thenReturn(Map.of("verified", true));
        when(rideRepository.save(any(Ride.class))).thenReturn(scheduledRide);

        RideResponse result = rideService.createRide(createRideRequest);

        assertThat(result).isNotNull();
        assertThat(result.getDriverId()).isEqualTo(2L);
        assertThat(result.getSource()).isEqualTo("Limerick");
        assertThat(result.getStatus()).isEqualTo(RideStatus.SCHEDULED);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    @DisplayName("createRide() - should throw when user is not a driver")
    void createRide_throwsException_whenNotDriver() {
        when(userServiceClient.verifyUser(2L, "DRIVER"))
                .thenReturn(Map.of("verified", false));

        assertThatThrownBy(() -> rideService.createRide(createRideRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not a verified driver");

        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRide() - should throw when User Service is down (fallback)")
    void createRide_throwsException_whenUserServiceDown() {
        when(userServiceClient.verifyUser(2L, "DRIVER"))
                .thenReturn(Map.of("fallback", true));

        assertThatThrownBy(() -> rideService.createRide(createRideRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User Service is currently unavailable");
    }

    // ── updateRide() ─────────────────────────────────────────

    @Test
    @DisplayName("updateRide() - should update scheduled ride successfully")
    void updateRide_success() {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(scheduledRide);

        CreateRideRequest updateRequest = CreateRideRequest.builder()
                .driverId(2L)
                .source("Cork")
                .destination("Galway")
                .departureDatetime(LocalDateTime.now().plusDays(2))
                .price(new BigDecimal("20.00"))
                .availableSeats(2)
                .build();

        RideResponse result = rideService.updateRide(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    @DisplayName("updateRide() - should throw when ride is not SCHEDULED")
    void updateRide_throwsException_whenNotScheduled() {
        scheduledRide.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));

        assertThatThrownBy(() -> rideService.updateRide(1L, createRideRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only scheduled rides can be updated");
    }

    // ── cancelRide() ─────────────────────────────────────────

    @Test
    @DisplayName("cancelRide() - should cancel ride and all confirmed bookings")
    void cancelRide_success() {
        Booking confirmedBooking = Booking.builder()
                .id(10L)
                .userId(3L)
                .ride(scheduledRide)
                .status(BookingStatus.CONFIRMED)
                .bookedSeats(1)
                .amount(new BigDecimal("15.00"))
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));
        when(bookingRepository.findByRideId(1L)).thenReturn(List.of(confirmedBooking));
        when(rideRepository.save(any(Ride.class))).thenReturn(scheduledRide);
        when(bookingRepository.save(any(Booking.class))).thenReturn(confirmedBooking);

        RideResponse result = rideService.cancelRide(1L);

        assertThat(result).isNotNull();
        assertThat(confirmedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    @DisplayName("cancelRide() - should throw when ride already cancelled")
    void cancelRide_throwsException_whenAlreadyCancelled() {
        scheduledRide.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));

        assertThatThrownBy(() -> rideService.cancelRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    // ── bookRide() ───────────────────────────────────────────

    @Test
    @DisplayName("bookRide() - should create booking and reduce available seats")
    void bookRide_success() {
        BookRideRequest request = BookRideRequest.builder()
                .userId(3L)
                .rideId(1L)
                .bookedSeats(2)
                .build();

        Booking booking = Booking.builder()
                .id(10L)
                .userId(3L)
                .ride(scheduledRide)
                .status(BookingStatus.CONFIRMED)
                .bookedSeats(2)
                .amount(new BigDecimal("30.00"))
                .build();

        when(userServiceClient.verifyUser(3L, "PASSENGER"))
                .thenReturn(Map.of("verified", true));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(rideRepository.save(any(Ride.class))).thenReturn(scheduledRide);

        BookingResponse result = rideService.bookRide(request);

        assertThat(result).isNotNull();
        assertThat(result.getBookedSeats()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        // Verify seats reduced
        assertThat(scheduledRide.getAvailableSeats()).isEqualTo(1);
    }

    @Test
    @DisplayName("bookRide() - should throw when not enough seats")
    void bookRide_throwsException_whenInsufficientSeats() {
        BookRideRequest request = BookRideRequest.builder()
                .userId(3L)
                .rideId(1L)
                .bookedSeats(10)  // more than available (3)
                .build();

        when(userServiceClient.verifyUser(3L, "PASSENGER"))
                .thenReturn(Map.of("verified", true));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));

        assertThatThrownBy(() -> rideService.bookRide(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough seats available");
    }

    @Test
    @DisplayName("bookRide() - should throw when ride is not SCHEDULED")
    void bookRide_throwsException_whenRideNotScheduled() {
        scheduledRide.setStatus(RideStatus.CANCELLED);
        BookRideRequest request = BookRideRequest.builder()
                .userId(3L)
                .rideId(1L)
                .bookedSeats(1)
                .build();

        when(userServiceClient.verifyUser(3L, "PASSENGER"))
                .thenReturn(Map.of("verified", true));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(scheduledRide));

        assertThatThrownBy(() -> rideService.bookRide(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available for booking");
    }

    // ── cancelBooking() ──────────────────────────────────────

    @Test
    @DisplayName("cancelBooking() - should cancel booking and restore seats")
    void cancelBooking_success() {
        Booking booking = Booking.builder()
                .id(10L)
                .userId(3L)
                .ride(scheduledRide)
                .status(BookingStatus.CONFIRMED)
                .bookedSeats(2)
                .amount(new BigDecimal("30.00"))
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(rideRepository.save(any(Ride.class))).thenReturn(scheduledRide);

        BookingResponse result = rideService.cancelBooking(10L);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        // Verify seats restored: 3 + 2 = 5
        assertThat(scheduledRide.getAvailableSeats()).isEqualTo(5);
    }

    @Test
    @DisplayName("cancelBooking() - should throw when booking already cancelled")
    void cancelBooking_throwsException_whenAlreadyCancelled() {
        Booking booking = Booking.builder()
                .id(10L)
                .ride(scheduledRide)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> rideService.cancelBooking(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    // ── getAverageRating() ───────────────────────────────────

    @Test
    @DisplayName("getAverageRating() - should return rounded average rating")
    void getAverageRating_success() {
        when(ratingRepository.findAverageRatingByUserId(3L)).thenReturn(4.666666);

        Double result = rideService.getAverageRating(3L);

        assertThat(result).isEqualTo(4.67);
    }

    @Test
    @DisplayName("getAverageRating() - should return 0.0 when no ratings exist")
    void getAverageRating_returnsZero_whenNoRatings() {
        when(ratingRepository.findAverageRatingByUserId(3L)).thenReturn(null);

        Double result = rideService.getAverageRating(3L);

        assertThat(result).isEqualTo(0.0);
    }
}