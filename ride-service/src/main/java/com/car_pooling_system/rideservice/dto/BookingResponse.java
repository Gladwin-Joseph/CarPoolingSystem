package com.car_pooling_system.rideservice.dto;

import com.car_pooling_system.rideservice.model.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long rideId;
    private BookingStatus status;
    private int bookedSeats;
    private BigDecimal amount;
    private LocalDateTime bookingDatetime;
}
