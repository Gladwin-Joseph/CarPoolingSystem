package com.car_pooling_system.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;

import java.math.BigDecimal;

public interface StripeService {
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, Long bookingId, Long userId) throws StripeException;
    PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException;
    Refund refundPayment(String paymentIntentId, BigDecimal amount) throws StripeException;
}