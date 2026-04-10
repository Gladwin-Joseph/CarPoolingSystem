package com.car_pooling_system.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "ride-service", fallback = RideServiceClientFallback.class)
public interface RideServiceClient {

    @GetMapping("/api/rides/bookings/{bookingId}")
    Map<String, Object> getBookingById(@PathVariable("bookingId") Long bookingId);

    @GetMapping("/api/rides/{rideId}")
    Map<String, Object> getRideById(@PathVariable("rideId") Long rideId);
}