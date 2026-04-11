package com.car_pooling_system.paymentservice.controller;

import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;
import com.car_pooling_system.paymentservice.service.PaymentService;
import com.car_pooling_system.paymentservice.service.StripeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Integration Tests (MockMvc)")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock all constructor deps of PaymentController
    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private StripeService stripeService; // <— added mock for missing bean

    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod("CARD")
                .status("COMPLETED")
                .transactionReference("TXN-ABCD1234")
                .build();
    }

    @Test
    @DisplayName("POST /api/payments - should return 201 on success")
    void processPayment_returns201() throws Exception {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod("CARD")
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionReference").value("TXN-ABCD1234"));
    }

    @Test
    @DisplayName("POST /api/payments - should return 400 when request body invalid")
    void processPayment_returns400_whenInvalidRequest() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest(); // missing required fields

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/payments/{paymentId}/refund - should return 200 on refund")
    void refundPayment_returns200() throws Exception {
        paymentResponse.setStatus("REFUNDED");
        when(paymentService.refundPayment(1L)).thenReturn(paymentResponse);

        mockMvc.perform(put("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("PUT /api/payments/{paymentId}/refund - should return 400 when payment not found")
    void refundPayment_returns400_whenFails() throws Exception {
        when(paymentService.refundPayment(99L))
                .thenThrow(new RuntimeException("Payment not found with id: 99"));

        mockMvc.perform(put("/api/payments/99/refund"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99"));
    }

    @Test
    @DisplayName("GET /api/payments/{paymentId} - should return 200 with payment data")
    void getPaymentStatus_returns200() throws Exception {
        when(paymentService.getPaymentStatus(1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));
    }

    @Test
    @DisplayName("GET /api/payments/booking/{bookingId} - should return payment for booking")
    void getPaymentByBookingId_returns200() throws Exception {
        when(paymentService.getPaymentByBookingId(10L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/booking/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(10));
    }

    @Test
    @DisplayName("GET /api/payments/user/{userId} - should return list of payments")
    void getPaymentsByUser_returns200() throws Exception {
        when(paymentService.getPaymentsByUser(3L)).thenReturn(List.of(paymentResponse));

        mockMvc.perform(get("/api/payments/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].payerUserId").value(3));
    }

    @Test
    @DisplayName("GET /api/payments/user/{userId} - should return empty array when no payments")
    void getPaymentsByUser_returnsEmptyList() throws Exception {
        when(paymentService.getPaymentsByUser(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/user/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}