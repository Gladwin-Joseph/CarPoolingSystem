package com.car_pooling_system.rideservice.observer;

import lombok.extern.slf4j.Slf4j;

/**
 * Concrete Observer: notifies the driver of ride events (booking, cancellation, etc.)
 */
@Slf4j
public class DriverObserver implements RideObserver {

    private final Long driverId;

    public DriverObserver(Long driverId) {
        this.driverId = driverId;
    }

    @Override
    public void update(String eventType, Long rideId, String message) {
        // In production, this could push notifications, send emails, etc.
        log.info("[DRIVER NOTIFICATION] Driver {} | Event: {} | Ride: {} | Message: {}",
                driverId, eventType, rideId, message);
    }

    public Long getDriverId() {
        return driverId;
    }
}
