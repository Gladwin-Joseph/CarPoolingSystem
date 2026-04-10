package com.car_pooling_system.rideservice.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete Subject: manages observers for each ride.
 * Uses a map of rideId -> list of observers so each ride has its own observer list.
 */
@Component
public class RideEventManager implements RideSubject {

    // Map of rideId to its list of observers
    private final Map<Long, List<RideObserver>> rideObservers = new ConcurrentHashMap<>();

    public void attach(Long rideId, RideObserver observer) {
        rideObservers.computeIfAbsent(rideId, k -> new ArrayList<>()).add(observer);
    }

    public void detach(Long rideId, RideObserver observer) {
        List<RideObserver> observers = rideObservers.get(rideId);
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void notifyObservers(Long rideId, String eventType, String message) {
        List<RideObserver> observers = rideObservers.get(rideId);
        if (observers != null) {
            for (RideObserver observer : observers) {
                observer.update(eventType, rideId, message);
            }
        }
    }

    // ── RideSubject interface methods (global) ──────────────

    @Override
    public void attach(RideObserver observer) {
        // Used for global observers if needed
    }

    @Override
    public void detach(RideObserver observer) {
        // Used for global observers if needed
    }

    @Override
    public void notifyObservers(String eventType, Long rideId, String message) {
        notifyObservers(rideId, eventType, message);
    }
}
