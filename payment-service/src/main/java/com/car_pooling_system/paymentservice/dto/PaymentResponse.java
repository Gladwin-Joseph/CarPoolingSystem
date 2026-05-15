package com.car_pooling_system.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Long payerUserId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String stripeClientSecret;   // ← NEW
}