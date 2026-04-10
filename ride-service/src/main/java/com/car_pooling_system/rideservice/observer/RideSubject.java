package com.car_pooling_system.rideservice.observer;

/**
 * Subject (from class diagram):
 * Manages a list of observers and notifies them of ride events.
 */
public interface RideSubject {
    void attach(RideObserver observer);
    void detach(RideObserver observer);
    void notifyObservers(String eventType, Long rideId, String message);
}
