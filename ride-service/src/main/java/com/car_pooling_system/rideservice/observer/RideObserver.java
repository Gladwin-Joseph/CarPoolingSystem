package com.car_pooling_system.rideservice.observer;

/**
 * Observer Pattern (from class diagram):
 * Observers are notified when a ride is created, updated, or cancelled.
 */
public interface RideObserver {
    void update(String eventType, Long rideId, String message);
}
