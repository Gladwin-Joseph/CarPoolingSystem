package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.client.RideServiceClient;
import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;
import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import com.car_pooling_system.paymentservice.repository.PaymentRepository;
import com.stripe.model.Refund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RideServiceClient rideServiceClient;
    @Mock private StripeService stripeService;          // ← ADDED

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment completedPayment;
    private ProcessPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        completedPayment = Payment.builder()
                .id(1L)
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .stripePaymentIntentId("pi_test_123")   // ← ADDED
                .transactionReference("TXN-ABCD1234")
                .build();

        paymentRequest = ProcessPaymentRequest.builder()
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod("CARD")
                .build();
    }

    // ── processPayment() (legacy) ────────────────────────────

    @Test
    @DisplayName("processPayment() - should process and complete payment successfully")
    void processPayment_success() {
        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("id", 10, "status", "CONFIRMED"));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment); // ← single save

        PaymentResponse result = paymentService.processPayment(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getBookingId()).isEqualTo(10L);
        assertThat(result.getTransactionReference()).startsWith("TXN-");
        verify(paymentRepository, times(1)).save(any(Payment.class)); // ← times(1)
    }

    @Test
    @DisplayName("processPayment() - should throw when Ride Service is down (fallback)")
    void processPayment_throwsException_whenRideServiceDown() {
        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("fallback", true));

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride Service is currently unavailable");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("processPayment() - should throw when payment already exists for booking")
    void processPayment_throwsException_whenDuplicatePayment() {
        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("id", 10, "status", "CONFIRMED"));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.of(completedPayment));

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment already exists for booking");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("processPayment() - should throw when payment method is invalid")
    void processPayment_throwsException_whenInvalidMethod() {
        paymentRequest.setPaymentMethod("BITCOIN");

        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("id", 10));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid payment method");
    }

    // ── refundPayment() ──────────────────────────────────────

    @Test
    @DisplayName("refundPayment() - should refund a completed payment via Stripe")
    void refundPayment_success() {
        Refund mockRefund = mock(Refund.class);
        when(mockRefund.getId()).thenReturn("re_test_abc123");

        Payment refundedPayment = Payment.builder()
                .id(1L).bookingId(10L)
                .amount(new BigDecimal("30.00"))
                .status(PaymentStatus.REFUNDED)
                .stripePaymentIntentId("pi_test_123")
                .stripeRefundId("re_test_abc123")
                .transactionReference("TXN-ABCD1234")
                .paymentMethod(PaymentMethod.CARD)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));
        when(stripeService.createRefund("pi_test_123")).thenReturn(mockRefund); // ← Stripe mock
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedPayment);

        PaymentResponse result = paymentService.refundPayment(1L);

        assertThat(result.getStatus()).isEqualTo("REFUNDED");
        verify(stripeService).createRefund("pi_test_123");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("refundPayment() - should throw when payment is not COMPLETED")
    void refundPayment_throwsException_whenNotCompleted() {
        completedPayment.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only COMPLETED payments can be refunded"); // ← fixed message
    }

    @Test
    @DisplayName("refundPayment() - should throw when payment not found")
    void refundPayment_throwsException_whenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refundPayment(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found with id: 99");
    }

    @Test
    @DisplayName("refundPayment() - should throw when no Stripe PaymentIntent ID on payment")
    void refundPayment_throwsException_whenNoStripeId() {
        Payment paymentNoStripe = Payment.builder()
                .id(2L)
                .status(PaymentStatus.COMPLETED)
                .stripePaymentIntentId(null)           // no Stripe ID
                .paymentMethod(PaymentMethod.CASH)
                .amount(new BigDecimal("20.00"))
                .build();

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(paymentNoStripe));

        assertThatThrownBy(() -> paymentService.refundPayment(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No Stripe PaymentIntent ID");

        verify(stripeService, never()).createRefund(any());
    }

    // ── getPaymentStatus() ───────────────────────────────────

    @Test
    @DisplayName("getPaymentStatus() - should return payment response")
    void getPaymentStatus_success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        PaymentResponse result = paymentService.getPaymentStatus(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("getPaymentStatus() - should throw when not found")
    void getPaymentStatus_throwsException_whenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentStatus(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found with id: 99");
    }

    // ── getPaymentByBookingId() ──────────────────────────────

    @Test
    @DisplayName("getPaymentByBookingId() - should return payment for booking")
    void getPaymentByBookingId_success() {
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.of(completedPayment));

        PaymentResponse result = paymentService.getPaymentByBookingId(10L);

        assertThat(result.getBookingId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getPaymentByBookingId() - should throw when not found")
    void getPaymentByBookingId_throwsException_whenNotFound() {
        when(paymentRepository.findByBookingId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByBookingId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for booking");
    }

    // ── getPaymentsByUser() ──────────────────────────────────

    @Test
    @DisplayName("getPaymentsByUser() - should return all payments for user")
    void getPaymentsByUser_success() {
        when(paymentRepository.findByPayerUserId(3L)).thenReturn(List.of(completedPayment));

        List<PaymentResponse> results = paymentService.getPaymentsByUser(3L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPayerUserId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getPaymentsByUser() - should return empty list when user has no payments")
    void getPaymentsByUser_returnsEmpty() {
        when(paymentRepository.findByPayerUserId(99L)).thenReturn(List.of());

        List<PaymentResponse> results = paymentService.getPaymentsByUser(99L);

        assertThat(results).isEmpty();
    }
}