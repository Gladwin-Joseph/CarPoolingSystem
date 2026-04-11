package com.car_pooling_system.paymentservice.controller;

import com.car_pooling_system.paymentservice.dto.*;
import com.car_pooling_system.paymentservice.service.PaymentService;
import com.car_pooling_system.paymentservice.service.StripeService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    // ── NEW: Stripe endpoints ────────────────────────────────

    /**
     * Step 1: React calls this to get a clientSecret.
     * React then uses Stripe.js to complete the payment with that clientSecret.
     *
     * POST /api/payments/create-intent
     * Body: { bookingId, payerUserId, amount, currency (optional) }
     * Returns: { paymentId, clientSecret, stripePaymentIntentId, ... }
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentIntent(request));
    }

    /**
     * Step 2: Stripe calls this webhook automatically when payment succeeds/fails.
     * You must register this URL in your Stripe Dashboard:
     *   https://dashboard.stripe.com/webhooks
     *   URL: https://your-server.com/api/payments/webhook
     *   Events to listen: payment_intent.succeeded, payment_intent.payment_failed
     *
     * NOTE: This endpoint must NOT require authentication (it's called by Stripe).
     * NOTE: Use raw body - do NOT use @RequestBody with parsed JSON here.
     */
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = stripeService.constructWebhookEvent(payload, sigHeader);
        } catch (RuntimeException e) {
            log.error("[WEBHOOK] Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid signature"));
        }

        // Extract the PaymentIntent from the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        if (dataObjectDeserializer.getObject().isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            String piId = paymentIntent.getId();

            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    log.info("[WEBHOOK] payment_intent.succeeded | PI: {}", piId);
                    paymentService.handlePaymentSuccess(piId);
                }
                case "payment_intent.payment_failed" -> {
                    log.warn("[WEBHOOK] payment_intent.payment_failed | PI: {}", piId);
                    paymentService.handlePaymentFailure(piId);
                }
                default -> log.info("[WEBHOOK] Unhandled event type: {}", event.getType());
            }
        }

        return ResponseEntity.ok(Map.of("received", "true"));
    }

    // ── Existing endpoints (unchanged) ──────────────────────

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request));
    }

    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }
}
