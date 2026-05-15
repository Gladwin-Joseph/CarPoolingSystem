package com.car_pooling_system.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeServiceImpl implements StripeService {

    @Override
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency,
                                             Long bookingId, Long userId) throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("bookingId", String.valueOf(bookingId));
        metadata.put("userId", String.valueOf(userId));

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putAllMetadata(metadata)
                .setDescription("Carpool ride booking #" + bookingId)
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("[STRIPE] Created PaymentIntent {} for booking {} amount EUR {}",
                intent.getId(), bookingId, amount);
        return intent;
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    @Override
    public Refund refundPayment(String paymentIntentId, BigDecimal amount) throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amountInCents)
                .build();

        Refund refund = Refund.create(params);
        log.info("[STRIPE] Refunded {} for PaymentIntent {}", refund.getId(), paymentIntentId);
        return refund;
    }
}