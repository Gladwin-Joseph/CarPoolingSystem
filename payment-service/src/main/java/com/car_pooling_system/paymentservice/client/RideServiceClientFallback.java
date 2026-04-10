package com.car_pooling_system.paymentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback: returns safe defaults when Ride Service is unavailable.
 */
@Component
@Slf4j
public class RideServiceClientFallback implements RideServiceClient {

    @Override
    public Map<String, Object> getBookingById(Long bookingId) {
        log.warn("[CIRCUIT BREAKER FALLBACK] Ride Service unavailable. " +
                "Cannot fetch booking {}. Returning empty response.", bookingId);
        return Map.of(
                "id", bookingId,
                "fallback", true,
                "message", "Ride Service is currently unavailable. Please try again later."
        );
    }

    @Override
    public Map<String, Object> getRideById(Long rideId) {
        log.warn("[CIRCUIT BREAKER FALLBACK] Ride Service unavailable. " +
                "Cannot fetch ride {}. Returning empty response.", rideId);
        return Map.of(
                "id", rideId,
                "fallback", true,
                "message", "Ride Service is currently unavailable. Please try again later."
        );
    }
}