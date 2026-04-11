package com.car_pooling_system.rideservice.controller;

import com.car_pooling_system.rideservice.dto.*;
import com.car_pooling_system.rideservice.model.BookingStatus;
import com.car_pooling_system.rideservice.model.RideStatus;
import com.car_pooling_system.rideservice.service.RideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@DisplayName("RideController Integration Tests (MockMvc)")
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    private ObjectMapper objectMapper;
    private RideResponse rideResponse;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        rideResponse = RideResponse.builder()
                .id(1L)
                .driverId(2L)
                .source("Limerick")
                .destination("Dublin")
                .departureDatetime(LocalDateTime.now().plusDays(1))
                .price(new BigDecimal("15.00"))
                .availableSeats(3)
                .status(RideStatus.SCHEDULED)
                .build();

        bookingResponse = BookingResponse.builder()
                .id(10L)
                .userId(3L)
                .rideId(1L)
                .status(BookingStatus.CONFIRMED)
                .bookedSeats(2)
                .amount(new BigDecimal("30.00"))
                .build();
    }

    // ── POST /api/rides ──────────────────────────────────────

    @Test
    @DisplayName("POST /api/rides - should return 201 on ride creation")
    void createRide_returns201() throws Exception {
        CreateRideRequest request = CreateRideRequest.builder()
                .driverId(2L)
                .source("Limerick")
                .destination("Dublin")
                .departureDatetime(LocalDateTime.now().plusDays(1))
                .price(new BigDecimal("15.00"))
                .availableSeats(3)
                .build();

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(rideResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.source").value("Limerick"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    // ── GET /api/rides/{rideId} ──────────────────────────────

    @Test
    @DisplayName("GET /api/rides/{rideId} - should return 200 with ride data")
    void getRideById_returns200() throws Exception {
        when(rideService.getRideById(1L)).thenReturn(rideResponse);

        mockMvc.perform(get("/api/rides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destination").value("Dublin"));
    }

    @Test
    @DisplayName("GET /api/rides/{rideId} - should return 400 when not found")
    void getRideById_returns400_whenNotFound() throws Exception {
        when(rideService.getRideById(99L))
                .thenThrow(new RuntimeException("Ride not found with id: 99"));

        mockMvc.perform(get("/api/rides/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Ride not found with id: 99"));
    }

    // ── PUT /api/rides/{rideId}/cancel ───────────────────────

    @Test
    @DisplayName("PUT /api/rides/{rideId}/cancel - should return 200 on cancel")
    void cancelRide_returns200() throws Exception {
        rideResponse.setStatus(RideStatus.CANCELLED);
        when(rideService.cancelRide(1L)).thenReturn(rideResponse);

        mockMvc.perform(put("/api/rides/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ── GET /api/rides/available ─────────────────────────────

    @Test
    @DisplayName("GET /api/rides/available - should return list of available rides")
    void getAllAvailableRides_returns200() throws Exception {
        when(rideService.getAllAvailableRides()).thenReturn(List.of(rideResponse));

        mockMvc.perform(get("/api/rides/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].availableSeats").value(3));
    }

    // ── GET /api/rides/driver/{driverId} ─────────────────────

    @Test
    @DisplayName("GET /api/rides/driver/{driverId} - should return driver's rides")
    void getHostedRides_returns200() throws Exception {
        when(rideService.getHostedRides(2L)).thenReturn(List.of(rideResponse));

        mockMvc.perform(get("/api/rides/driver/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].driverId").value(2));
    }

    // ── POST /api/rides/bookings ─────────────────────────────

    @Test
    @DisplayName("POST /api/rides/bookings - should return 201 on booking")
    void bookRide_returns201() throws Exception {
        BookRideRequest request = BookRideRequest.builder()
                .userId(3L)
                .rideId(1L)
                .bookedSeats(2)
                .build();

        when(rideService.bookRide(any(BookRideRequest.class))).thenReturn(bookingResponse);

        mockMvc.perform(post("/api/rides/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.bookedSeats").value(2));
    }

    // ── PUT /api/rides/bookings/{bookingId}/cancel ───────────

    @Test
    @DisplayName("PUT /bookings/{bookingId}/cancel - should return 200 on cancel")
    void cancelBooking_returns200() throws Exception {
        bookingResponse.setStatus(BookingStatus.CANCELLED);
        when(rideService.cancelBooking(10L)).thenReturn(bookingResponse);

        mockMvc.perform(put("/api/rides/bookings/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ── GET /api/rides/ratings/user/{userId}/average ─────────

    @Test
    @DisplayName("GET /ratings/user/{userId}/average - should return average rating")
    void getAverageRating_returns200() throws Exception {
        when(rideService.getAverageRating(3L)).thenReturn(4.5);

        mockMvc.perform(get("/api/rides/ratings/user/3/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }
}