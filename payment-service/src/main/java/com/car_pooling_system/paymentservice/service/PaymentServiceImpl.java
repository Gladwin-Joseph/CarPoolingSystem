package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.client.RideServiceClient;
import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;
import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import com.car_pooling_system.paymentservice.repository.PaymentRepository;
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

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Verify booking exists via Ride Service (Feign + Circuit Breaker)
        Map<String, Object> booking = rideServiceClient.getBookingById(request.getBookingId());

        // Check if this is a fallback response (Ride Service down)
        if (booking.containsKey("fallback") && (Boolean) booking.get("fallback")) {
            log.warn("[PAYMENT] Ride Service unavailable. Cannot process payment for booking {}.",
                    request.getBookingId());
            throw new RuntimeException("Ride Service is currently unavailable. Cannot verify booking. Please try again later.");
        }

        if (booking == null || booking.isEmpty()) {
            throw new RuntimeException("Booking not found with id: " + request.getBookingId());
        }

        // Check if payment already exists for this booking
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("Payment already exists for booking: " + request.getBookingId());
        }

        // Parse payment method
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method: " + request.getPaymentMethod());
        }

        // Create payment
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .payerUserId(request.getPayerUserId())
                .amount(request.getAmount())
                .paymentMethod(method)
                .status(PaymentStatus.PENDING)
                .transactionReference(generateTransactionReference())
                .build();

        payment = paymentRepository.save(payment);

        // Simulate payment processing
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        log.info("[PAYMENT PROCESSED] Payment #{} | Booking #{} | Amount: {} | Method: {} | Ref: {}",
                payment.getId(), payment.getBookingId(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getTransactionReference());

        return toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        log.info("[PAYMENT REFUNDED] Payment #{} | Booking #{} | Amount: {} | Ref: {}",
                payment.getId(), payment.getBookingId(), payment.getAmount(),
                payment.getTransactionReference());

        return toPaymentResponse(payment);
    }

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

    // ── Helpers ─────────────────────────────────────────────

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
                .build();
    }
}
