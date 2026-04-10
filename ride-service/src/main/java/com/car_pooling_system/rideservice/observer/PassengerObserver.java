package com.car_pooling_system.rideservice.observer;

import lombok.extern.slf4j.Slf4j;

/**
 * Concrete Observer: notifies a passenger of ride events (ride updates, cancellation, etc.)
 */
@Slf4j
public class PassengerObserver implements RideObserver {

    private final Long passengerId;

    public PassengerObserver(Long passengerId) {
        this.passengerId = passengerId;
    }

    @Override
    public void update(String eventType, Long rideId, String message) {
        log.info("[PASSENGER NOTIFICATION] Passenger {} | Event: {} | Ride: {} | Message: {}",
                passengerId, eventType, rideId, message);
    }

    public Long getPassengerId() {
        return passengerId;
    }
}
