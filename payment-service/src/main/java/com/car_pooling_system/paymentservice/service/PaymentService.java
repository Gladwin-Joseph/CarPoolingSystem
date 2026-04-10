package com.car_pooling_system.paymentservice.service;

import com.car_pooling_system.paymentservice.dto.PaymentResponse;
import com.car_pooling_system.paymentservice.dto.ProcessPaymentRequest;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(ProcessPaymentRequest request);
    PaymentResponse refundPayment(Long paymentId);
    PaymentResponse getPaymentStatus(Long paymentId);
    PaymentResponse getPaymentByBookingId(Long bookingId);
    List<PaymentResponse> getPaymentsByUser(Long userId);
}
