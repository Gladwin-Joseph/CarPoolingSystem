package com.car_pooling_system.paymentservice.controller;

import com.car_pooling_system.paymentservice.dto.*;
import com.car_pooling_system.paymentservice.service.PaymentService;
import com.car_pooling_system.paymentservice.service.StripeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
@DisplayName("PaymentController Stripe Integration Tests")
class PaymentControllerStripeTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PaymentService paymentService;
    @MockitoBean private StripeService stripeService;

    private PaymentIntentResponse paymentIntentResponse;

    @BeforeEach
    void setUp() {
        paymentIntentResponse = PaymentIntentResponse.builder()
                .paymentId(1L)
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .currency("eur")
                .clientSecret("pi_test_123_secret_abc")
                .stripePaymentIntentId("pi_test_123")
                .status("PENDING")
                .build();
    }

    // ── POST /api/payments/create-intent ─────────────────────

    @Test
    @DisplayName("POST /create-intent - should return 201 with clientSecret")
    void createPaymentIntent_returns201() throws Exception {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00")).build();

        when(paymentService.createPaymentIntent(any(CreatePaymentIntentRequest.class)))
                .thenReturn(paymentIntentResponse);

        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientSecret").value("pi_test_123_secret_abc"))
                .andExpect(jsonPath("$.stripePaymentIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.currency").value("eur"))
                .andExpect(jsonPath("$.bookingId").value(10));
    }

    @Test
    @DisplayName("POST /create-intent - should return 400 when request missing required fields")
    void createPaymentIntent_returns400_whenInvalid() throws Exception {
        CreatePaymentIntentRequest request = new CreatePaymentIntentRequest();

        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /create-intent - should return 400 when Ride Service unavailable")
    void createPaymentIntent_returns400_whenRideServiceDown() throws Exception {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .bookingId(10L).payerUserId(3L)
                .amount(new BigDecimal("30.00")).build();

        when(paymentService.createPaymentIntent(any()))
                .thenThrow(new RuntimeException("Ride Service is currently unavailable"));

        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/payments/webhook ───────────────────────────

    @Test
    @DisplayName("POST /webhook - should handle payment_intent.succeeded and return 200")
    void webhook_handlesPaymentSuccess() throws Exception {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("payment_intent.succeeded");

        PaymentIntent mockPI = mock(PaymentIntent.class);
        when(mockPI.getId()).thenReturn("pi_test_123");

        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPI));
        when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

        when(stripeService.constructWebhookEvent(anyString(), anyString())).thenReturn(mockEvent);

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "test_sig")
                        .content("{\"type\":\"payment_intent.succeeded\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value("true"));

        verify(paymentService).handlePaymentSuccess("pi_test_123");
    }

    @Test
    @DisplayName("POST /webhook - should handle payment_intent.payment_failed and return 200")
    void webhook_handlesPaymentFailure() throws Exception {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");

        PaymentIntent mockPI = mock(PaymentIntent.class);
        when(mockPI.getId()).thenReturn("pi_test_123");

        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPI));
        when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

        when(stripeService.constructWebhookEvent(anyString(), anyString())).thenReturn(mockEvent);

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "test_sig")
                        .content("{\"type\":\"payment_intent.payment_failed\"}"))
                .andExpect(status().isOk());

        verify(paymentService).handlePaymentFailure("pi_test_123");
    }

    @Test
    @DisplayName("POST /webhook - should return 400 on invalid Stripe signature")
    void webhook_returns400_onInvalidSignature() throws Exception {
        when(stripeService.constructWebhookEvent(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid Stripe webhook signature"));

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "invalid_sig")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid signature"));
    }

    // ── PUT /api/payments/{paymentId}/refund ─────────────────

    @Test
    @DisplayName("PUT /refund - should return 200 with REFUNDED status")
    void refundPayment_returns200() throws Exception {
        PaymentResponse refundedResponse = PaymentResponse.builder()
                .id(1L).bookingId(10L)
                .status("REFUNDED")
                .stripePaymentIntentId("pi_test_123")
                .stripeRefundId("re_test_abc123")
                .amount(new BigDecimal("30.00"))
                .paymentMethod("CARD")
                .build();

        when(paymentService.refundPayment(1L)).thenReturn(refundedResponse);

        mockMvc.perform(put("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.stripeRefundId").value("re_test_abc123"));
    }

    @Test
    @DisplayName("PUT /refund - should return 400 when payment not COMPLETED")
    void refundPayment_returns400_whenNotCompleted() throws Exception {
        when(paymentService.refundPayment(1L))
                .thenThrow(new RuntimeException("Only COMPLETED payments can be refunded"));

        mockMvc.perform(put("/api/payments/1/refund"))
                .andExpect(status().isBadRequest());
    }
}
