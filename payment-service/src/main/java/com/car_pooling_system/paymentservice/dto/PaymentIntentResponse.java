package com.car_pooling_system.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponse {

    private Long paymentId;          // Your internal DB payment ID
    private Long bookingId;
    private Long payerUserId;
    private BigDecimal amount;
    private String currency;
    private String clientSecret;      // ← Pass this to Stripe.js on the frontend
    private String stripePaymentIntentId;
    private String status;           // PENDING initially
}