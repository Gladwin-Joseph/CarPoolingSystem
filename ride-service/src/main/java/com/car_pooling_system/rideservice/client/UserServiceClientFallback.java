package com.car_pooling_system.rideservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback: returns safe defaults when User Service is unavailable.
 * Circuit breaker routes here when the circuit is OPEN or calls timeout.
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Map<String, Object> verifyUser(Long userId, String userType) {
        log.warn("[CIRCUIT BREAKER FALLBACK] User Service unavailable. " +
                "Cannot verify user {} as {}. Returning unverified.", userId, userType);
        return Map.of(
                "userId", userId,
                "userType", userType,
                "verified", false,
                "fallback", true,
                "message", "User Service is currently unavailable. Please try again later."
        );
    }

    @Override
    public Map<String, Object> getUserById(Long id) {
        log.warn("[CIRCUIT BREAKER FALLBACK] User Service unavailable. " +
                "Cannot fetch user {}. Returning empty response.", id);
        return Map.of(
                "id", id,
                "fallback", true,
                "message", "User Service is currently unavailable. Please try again later."
        );
    }
}