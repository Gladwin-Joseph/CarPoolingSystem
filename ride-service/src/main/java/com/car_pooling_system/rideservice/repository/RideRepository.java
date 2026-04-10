package com.car_pooling_system.rideservice.repository;

import com.car_pooling_system.rideservice.model.Ride;
import com.car_pooling_system.rideservice.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriverId(Long driverId);

    List<Ride> findByStatus(RideStatus status);

    // Search rides by source, destination, and date (from class diagram: showRides)
    @Query("SELECT r FROM Ride r WHERE " +
           "LOWER(r.source) LIKE LOWER(CONCAT('%', :source, '%')) AND " +
           "LOWER(r.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
           "r.departureDatetime >= :date AND " +
           "r.availableSeats > 0 AND " +
           "r.status = 'SCHEDULED'")
    List<Ride> searchRides(@Param("source") String source,
                           @Param("destination") String destination,
                           @Param("date") LocalDateTime date);

    // Show all available rides
    @Query("SELECT r FROM Ride r WHERE r.availableSeats > 0 AND r.status = 'SCHEDULED' " +
           "AND r.departureDatetime > CURRENT_TIMESTAMP ORDER BY r.departureDatetime ASC")
    List<Ride> findAllAvailableRides();
}
