package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.client.RideServiceClient;
import com.car_pooling_system.paymentservice.dto.CreatePaymentIntentRequest;
import com.car_pooling_system.paymentservice.dto.PaymentIntentResponse;
import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;
import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import com.car_pooling_system.paymentservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        Map<String, Object> booking = rideServiceClient.getBookingById(request.getBookingId());

        if (booking.containsKey("fallback") && (Boolean) booking.get("fallback")) {
            throw new RuntimeException("Ride Service is currently unavailable. Cannot verify booking.");
        }
        if (booking == null || booking.isEmpty()) {
            throw new RuntimeException("Booking not found: " + request.getBookingId());
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
                .status(PaymentStatus.PENDING)
                .build();

        if (method == PaymentMethod.CARD) {
            try {
                PaymentIntent intent = stripeService.createPaymentIntent(
                        request.getAmount(), "eur",
                        request.getBookingId(), request.getPayerUserId()
                );
                payment.setStripePaymentIntentId(intent.getId());
                payment.setStripeClientSecret(intent.getClientSecret());
                payment.setTransactionReference(intent.getId());
                payment.setStatus(PaymentStatus.COMPLETED);
                log.info("[STRIPE PAYMENT] PaymentIntent {} created for booking {}",
                        intent.getId(), request.getBookingId());
            } catch (StripeException e) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.error("[STRIPE ERROR] {}", e.getMessage());
                throw new RuntimeException("Stripe payment failed: " + e.getMessage());
            }
        } else {
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setStatus(PaymentStatus.PENDING);
        }

        payment = paymentRepository.save(payment);
        log.info("[PAYMENT] Saved payment #{} | Method: {} | Amount: EUR {}",
                payment.getId(), payment.getPaymentMethod(), payment.getAmount());

        return toPaymentResponse(payment);
    }

    @Override
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        return null;
    }

    @Override
    public void handlePaymentSuccess(String stripePaymentIntentId) {

    }

    @Override
    public void handlePaymentFailure(String stripePaymentIntentId) {

    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        if (payment.getStripePaymentIntentId() != null) {
            try {
                Refund refund = stripeService.refundPayment(
                        payment.getStripePaymentIntentId(),
                        payment.getAmount()
                );
                log.info("[STRIPE REFUND] {} for payment #{}", refund.getId(), payment.getId());
            } catch (StripeException e) {
                log.error("[STRIPE REFUND ERROR] {}", e.getMessage());
                throw new RuntimeException("Stripe refund failed: " + e.getMessage());
            }
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);
        return toPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
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

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .payerUserId(payment.getPayerUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus().name())
                .transactionReference(payment.getTransactionReference())
                .paymentDate(payment.getPaymentDate())
                .stripeClientSecret(payment.getStripeClientSecret())  // ← NEW
                .build();
    }
}