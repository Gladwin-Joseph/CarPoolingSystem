package com.car_pooling_system.paymentservice.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("[STRIPE] Stripe SDK initialized.");
    }

    @Override
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency,
                                             Long bookingId, Long payerUserId) {
        try {
            // Stripe amounts are in smallest currency unit (cents/pence)
            // e.g. €15.00 → 1500 cents
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("bookingId", String.valueOf(bookingId));
            metadata.put("payerUserId", String.valueOf(payerUserId));

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                    )
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("[STRIPE] PaymentIntent created: {} | Amount: {} {} | Booking: {}",
                    paymentIntent.getId(), amount, currency, bookingId);
            return paymentIntent;

        } catch (StripeException e) {
            log.error("[STRIPE] Failed to create PaymentIntent: {}", e.getMessage());
            throw new RuntimeException("Failed to create Stripe PaymentIntent: " + e.getMessage());
        }
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            log.error("[STRIPE] Failed to retrieve PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PaymentIntent: " + e.getMessage());
        }
    }

    @Override
    public Refund createRefund(String paymentIntentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            log.info("[STRIPE] Refund created: {} for PaymentIntent: {}", refund.getId(), paymentIntentId);
            return refund;

        } catch (StripeException e) {
            log.error("[STRIPE] Failed to create refund for {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Failed to create Stripe refund: " + e.getMessage());
        }
    }

    @Override
    public Event constructWebhookEvent(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("[STRIPE] Webhook signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Stripe webhook signature");
        }
    }
}