package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.client.RideServiceClient;
import com.car_pooling_system.paymentservice.dto.CreatePaymentIntentRequest;
import com.car_pooling_system.paymentservice.dto.PaymentIntentResponse;
import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import com.car_pooling_system.paymentservice.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Stripe Unit Tests")
class PaymentServiceStripeTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RideServiceClient rideServiceClient;
    @Mock private StripeService stripeService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment pendingPayment;
    private Payment completedPayment;

    @BeforeEach
    void setUp() {
        // Inject the @Value field
        ReflectionTestUtils.setField(paymentService, "defaultCurrency", "eur");

        pendingPayment = Payment.builder()
                .id(1L).bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_test_123")
                .stripeClientSecret("pi_test_123_secret_abc")
                .transactionReference("TXN-ABCD1234")
                .build();

        completedPayment = Payment.builder()
                .id(1L).bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .stripePaymentIntentId("pi_test_123")
                .transactionReference("TXN-ABCD1234")
                .build();
    }

    // ── createPaymentIntent() ────────────────────────────────

    @Test
    @DisplayName("createPaymentIntent() - should create Stripe PaymentIntent and save PENDING payment")
    void createPaymentIntent_success() {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00")).build();

        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("id", 10, "status", "CONFIRMED"));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.empty());

        // Mock Stripe PaymentIntent
        PaymentIntent mockPI = mock(PaymentIntent.class);
        when(mockPI.getId()).thenReturn("pi_test_123");
        when(mockPI.getClientSecret()).thenReturn("pi_test_123_secret_abc");
        when(stripeService.createPaymentIntent(
                any(BigDecimal.class), eq("eur"), eq(10L), eq(3L)))
                .thenReturn(mockPI);
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        PaymentIntentResponse result = paymentService.createPaymentIntent(request);

        assertThat(result).isNotNull();
        assertThat(result.getClientSecret()).isEqualTo("pi_test_123_secret_abc");
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getBookingId()).isEqualTo(10L);
        verify(stripeService).createPaymentIntent(any(), eq("eur"), eq(10L), eq(3L));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPaymentIntent() - should use custom currency when provided")
    void createPaymentIntent_usesCustomCurrency() {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .currency("usd").build();

        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("id", 10));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.empty());

        PaymentIntent mockPI = mock(PaymentIntent.class);
        when(mockPI.getId()).thenReturn("pi_test_456");
        when(mockPI.getClientSecret()).thenReturn("pi_test_456_secret");
        when(stripeService.createPaymentIntent(any(), eq("usd"), anyLong(), anyLong()))
                .thenReturn(mockPI);
        when(paymentRepository.save(any())).thenReturn(pendingPayment);

        paymentService.createPaymentIntent(request);

        verify(stripeService).createPaymentIntent(any(), eq("usd"), anyLong(), anyLong());
    }

    @Test
    @DisplayName("createPaymentIntent() - should throw when Ride Service is down")
    void createPaymentIntent_throwsWhenRideServiceDown() {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00")).build();

        when(rideServiceClient.getBookingById(10L))
                .thenReturn(Map.of("fallback", true));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unavailable");

        verify(stripeService, never()).createPaymentIntent(any(), any(), any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPaymentIntent() - should throw when payment already exists for booking")
    void createPaymentIntent_throwsWhenDuplicateBooking() {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00")).build();

        when(rideServiceClient.getBookingById(10L)).thenReturn(Map.of("id", 10));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.of(completedPayment));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(stripeService, never()).createPaymentIntent(any(), any(), any(), any());
    }

    // ── handlePaymentSuccess() ───────────────────────────────

    @Test
    @DisplayName("handlePaymentSuccess() - should set payment status to COMPLETED")
    void handlePaymentSuccess_updatesStatusToCompleted() {
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        paymentService.handlePaymentSuccess("pi_test_123");

        assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository).save(pendingPayment);
    }

    @Test
    @DisplayName("handlePaymentSuccess() - should throw when PaymentIntent not found")
    void handlePaymentSuccess_throwsWhenNotFound() {
        when(paymentRepository.findByStripePaymentIntentId("pi_unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handlePaymentSuccess("pi_unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }

    // ── handlePaymentFailure() ───────────────────────────────

    @Test
    @DisplayName("handlePaymentFailure() - should set payment status to FAILED")
    void handlePaymentFailure_updatesStatusToFailed() {
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        paymentService.handlePaymentFailure("pi_test_123");

        assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(pendingPayment);
    }

    // ── refundPayment() ──────────────────────────────────────

    @Test
    @DisplayName("refundPayment() - should call Stripe refund and update status to REFUNDED")
    void refundPayment_success() {
        Refund mockRefund = mock(Refund.class);
        when(mockRefund.getId()).thenReturn("re_test_abc123");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));
        when(stripeService.createRefund("pi_test_123")).thenReturn(mockRefund);
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        PaymentResponse result = paymentService.refundPayment(1L);

        assertThat(completedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(completedPayment.getStripeRefundId()).isEqualTo("re_test_abc123");
        verify(stripeService).createRefund("pi_test_123");
        verify(paymentRepository).save(completedPayment);
    }

    @Test
    @DisplayName("refundPayment() - should throw when payment is not COMPLETED")
    void refundPayment_throwsWhenNotCompleted() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only COMPLETED payments can be refunded");

        verify(stripeService, never()).createRefund(any());
    }

    @Test
    @DisplayName("refundPayment() - should throw when no Stripe PI ID on payment")
    void refundPayment_throwsWhenNoStripeId() {
        Payment paymentNoStripe = Payment.builder()
                .id(2L).status(PaymentStatus.COMPLETED)
                .stripePaymentIntentId(null)
                .paymentMethod(PaymentMethod.CASH)
                .amount(new BigDecimal("20.00")).build();

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(paymentNoStripe));

        assertThatThrownBy(() -> paymentService.refundPayment(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No Stripe PaymentIntent ID");

        verify(stripeService, never()).createRefund(any());
    }

    @Test
    @DisplayName("refundPayment() - should throw when payment not found")
    void refundPayment_throwsWhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refundPayment(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found with id: 99");
    }
}