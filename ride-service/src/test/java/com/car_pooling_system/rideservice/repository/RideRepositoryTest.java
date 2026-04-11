package com.car_pooling_system.rideservice.repository;

import com.car_pooling_system.rideservice.model.Ride;
import com.car_pooling_system.rideservice.model.RideStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RideRepository Integration Tests")
class RideRepositoryTest {

    @Autowired
    private RideRepository rideRepository;

    private Ride savedRide;

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        savedRide = rideRepository.save(Ride.builder()
                .driverId(2L)
                .source("Limerick")
                .destination("Dublin")
                .departureDatetime(LocalDateTime.now().plusDays(1))
                .arrivalDatetime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("15.00"))
                .availableSeats(3)
                .status(RideStatus.SCHEDULED)
                .build());
    }

    @Test
    @DisplayName("findByDriverId() - should return rides for the given driver")
    void findByDriverId_returnsRides() {
        List<Ride> rides = rideRepository.findByDriverId(2L);

        assertThat(rides).hasSize(1);
        assertThat(rides.get(0).getSource()).isEqualTo("Limerick");
    }

    @Test
    @DisplayName("findByDriverId() - should return empty list for unknown driver")
    void findByDriverId_returnsEmpty_whenNoRides() {
        List<Ride> rides = rideRepository.findByDriverId(999L);

        assertThat(rides).isEmpty();
    }

    @Test
    @DisplayName("findByStatus() - should return only SCHEDULED rides")
    void findByStatus_returnsScheduledRides() {
        List<Ride> scheduled = rideRepository.findByStatus(RideStatus.SCHEDULED);

        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.get(0).getStatus()).isEqualTo(RideStatus.SCHEDULED);
    }

    @Test
    @DisplayName("findAllAvailableRides() - should return rides with seats and SCHEDULED status")
    void findAllAvailableRides_returnsResults() {
        List<Ride> available = rideRepository.findAllAvailableRides();

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getAvailableSeats()).isGreaterThan(0);
    }

    @Test
    @DisplayName("findAllAvailableRides() - should not return fully booked rides")
    void findAllAvailableRides_excludesFullRides() {
        rideRepository.save(Ride.builder()
                .driverId(2L)
                .source("Cork")
                .destination("Kerry")
                .departureDatetime(LocalDateTime.now().plusDays(2))
                .price(new BigDecimal("10.00"))
                .availableSeats(0)  // fully booked
                .status(RideStatus.SCHEDULED)
                .build());

        List<Ride> available = rideRepository.findAllAvailableRides();

        assertThat(available)
                .isNotEmpty()
                .allMatch(r -> r.getAvailableSeats() > 0);
    }

    @Test
    @DisplayName("searchRides() - should find rides matching source, destination and date")
    void searchRides_returnsMatches() {
        List<Ride> results = rideRepository.searchRides(
                "Limerick", "Dublin", LocalDateTime.now());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSource()).containsIgnoringCase("Limerick");
    }

    @Test
    @DisplayName("searchRides() - should return empty list when no match")
    void searchRides_returnsEmpty_whenNoMatch() {
        List<Ride> results = rideRepository.searchRides(
                "Galway", "Cork", LocalDateTime.now());

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("searchRides() - should not return cancelled rides")
    void searchRides_excludesCancelledRides() {
        savedRide.setStatus(RideStatus.CANCELLED);
        rideRepository.save(savedRide);

        List<Ride> results = rideRepository.searchRides(
                "Limerick", "Dublin", LocalDateTime.now());

        assertThat(results).isEmpty();
    }
}