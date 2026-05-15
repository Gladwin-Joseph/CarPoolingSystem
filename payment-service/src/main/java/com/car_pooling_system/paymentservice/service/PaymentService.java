package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.dto.CreatePaymentIntentRequest;
import com.car_pooling_system.paymentservice.dto.PaymentIntentResponse;
import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;

import java.util.List;

public interface PaymentService {

    // ── Stripe-based methods (NEW) ───────────────────────────

    /** Step 1 of payment: Create a Stripe PaymentIntent.
     *  Returns clientSecret for React frontend to complete payment. */
    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);

    /** Called by Stripe webhook when payment_intent.succeeded event fires. */
    void handlePaymentSuccess(String stripePaymentIntentId);

    /** Called by Stripe webhook when payment_intent.payment_failed event fires. */
    void handlePaymentFailure(String stripePaymentIntentId);

    // ── Existing methods ─────────────────────────────────────

    /** Refunds a completed payment via Stripe Refunds API. */
    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getPaymentStatus(Long paymentId);
    PaymentResponse getPaymentByBookingId(Long bookingId);
    List<PaymentResponse> getPaymentsByUser(Long userId);

    // ── Legacy (kept for backward compatibility) ─────────────
    PaymentResponse processPayment(ProcessPaymentRequest request);
}