package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.client.RideServiceClient;
import com.car_pooling_system.paymentservice.dto.*;
import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import com.car_pooling_system.paymentservice.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideServiceClient rideServiceClient;
    private final StripeService stripeService;

    @Value("${stripe.currency:eur}")
    private String defaultCurrency;

    // ── STRIPE: Create PaymentIntent ─────────────────────────

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        // 1. Verify booking exists via Ride Service
        Map<String, Object> booking = rideServiceClient.getBookingById(request.getBookingId());

        if (booking.containsKey("fallback") && (Boolean) booking.get("fallback")) {
            throw new RuntimeException("Ride Service is currently unavailable. Please try again later.");
        }
        if (booking == null || booking.isEmpty()) {
            throw new RuntimeException("Booking not found with id: " + request.getBookingId());
        }

        // 2. Prevent duplicate payments for the same booking
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("A payment already exists for booking: " + request.getBookingId());
        }

        // 3. Determine currency
        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency()
                : defaultCurrency;

        // 4. Create Stripe PaymentIntent
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                request.getAmount(), currency,
                request.getBookingId(), request.getPayerUserId()
        );

        // 5. Save payment record as PENDING
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .payerUserId(request.getPayerUserId())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.CARD)   // Stripe = card by default
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId(paymentIntent.getId())
                .stripeClientSecret(paymentIntent.getClientSecret())
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        payment = paymentRepository.save(payment);

        log.info("[STRIPE] PaymentIntent created for booking #{} | PI: {} | Amount: {} {}",
                request.getBookingId(), paymentIntent.getId(), request.getAmount(), currency);

        return PaymentIntentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .payerUserId(payment.getPayerUserId())
                .amount(payment.getAmount())
                .currency(currency)
                .clientSecret(paymentIntent.getClientSecret())
                .stripePaymentIntentId(paymentIntent.getId())
                .status(payment.getStatus().name())
                .build();
    }

    // ── STRIPE: Webhook handlers ─────────────────────────────

    @Override
    @Transactional
    public void handlePaymentSuccess(String stripePaymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for Stripe PI: " + stripePaymentIntentId));

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        log.info("[STRIPE WEBHOOK] Payment COMPLETED | PI: {} | Booking #{} | Amount: {}",
                stripePaymentIntentId, payment.getBookingId(), payment.getAmount());
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String stripePaymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for Stripe PI: " + stripePaymentIntentId));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.warn("[STRIPE WEBHOOK] Payment FAILED | PI: {} | Booking #{}",
                stripePaymentIntentId, payment.getBookingId());
    }

    // ── STRIPE: Refund ───────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException(
                    "Only COMPLETED payments can be refunded. Current status: " + payment.getStatus());
        }

        if (payment.getStripePaymentIntentId() == null) {
            throw new RuntimeException("No Stripe PaymentIntent ID associated with payment #" + paymentId);
        }

        // Call Stripe Refunds API
        Refund refund = stripeService.createRefund(payment.getStripePaymentIntentId());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setStripeRefundId(refund.getId());
        payment = paymentRepository.save(payment);

        log.info("[STRIPE] Refund ISSUED | Payment #{} | Refund: {} | Booking #{}",
                paymentId, refund.getId(), payment.getBookingId());

        return toPaymentResponse(payment);
    }

    // ── Getters ──────────────────────────────────────────────

    @Override
    public PaymentResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        return toPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking: " + bookingId));
        return toPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByPayerUserId(userId).stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    // ── Legacy (backward compat) ─────────────────────────────

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Legacy endpoint - kept for backward compatibility
        // New code should use createPaymentIntent() instead
        Map<String, Object> booking = rideServiceClient.getBookingById(request.getBookingId());

        if (booking.containsKey("fallback") && (Boolean) booking.get("fallback")) {
            throw new RuntimeException("Ride Service is currently unavailable.");
        }
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("Payment already exists for booking: " + request.getBookingId());
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method: " + request.getPaymentMethod());
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .payerUserId(request.getPayerUserId())
                .amount(request.getAmount())
                .paymentMethod(method)
                .status(PaymentStatus.COMPLETED)
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        payment = paymentRepository.save(payment);
        return toPaymentResponse(payment);
    }

    // ── Mapper ───────────────────────────────────────────────

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .payerUserId(payment.getPayerUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus().name())
                .transactionReference(payment.getTransactionReference())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeRefundId(payment.getStripeRefundId())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}