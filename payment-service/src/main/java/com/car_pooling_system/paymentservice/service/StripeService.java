package com.car_pooling_system.paymentservice.service;

import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;

import java.math.BigDecimal;

public interface StripeService {

    /**
     * Creates a Stripe PaymentIntent.
     * Returns the PaymentIntent object containing clientSecret.
     * The clientSecret is sent to React frontend to complete the payment.
     */
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, Long bookingId, Long payerUserId);

    /**
     * Retrieves a PaymentIntent from Stripe by its ID.
     */
    PaymentIntent retrievePaymentIntent(String paymentIntentId);

    /**
     * Creates a full refund for a given Stripe PaymentIntent.
     * Returns the Refund object containing the refund ID.
     */
    Refund createRefund(String paymentIntentId);

    /**
     * Validates a Stripe webhook signature.
     * Throws SignatureVerificationException if invalid.
     */
    com.stripe.model.Event constructWebhookEvent(String payload, String sigHeader);
}