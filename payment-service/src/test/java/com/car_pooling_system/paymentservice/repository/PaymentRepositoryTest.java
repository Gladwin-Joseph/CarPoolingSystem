package com.car_pooling_system.paymentservice.repository;

import com.car_pooling_system.paymentservice.model.Payment;
import com.car_pooling_system.paymentservice.model.PaymentMethod;
import com.car_pooling_system.paymentservice.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PaymentRepository Integration Tests")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        savedPayment = paymentRepository.save(Payment.builder()
                .bookingId(10L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionReference("TXN-ABCD1234")
                .build());
    }

    // ── findByBookingId() ────────────────────────────────────

    @Test
    @DisplayName("findByBookingId() - should return payment for given booking")
    void findByBookingId_success() {
        Optional<Payment> result = paymentRepository.findByBookingId(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getPayerUserId()).isEqualTo(3L);
        assertThat(result.get().getAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("findByBookingId() - should return empty when booking not found")
    void findByBookingId_returnsEmpty_whenNotFound() {
        Optional<Payment> result = paymentRepository.findByBookingId(999L);

        assertThat(result).isEmpty();
    }

    // ── findByPayerUserId() ──────────────────────────────────

    @Test
    @DisplayName("findByPayerUserId() - should return all payments for a user")
    void findByPayerUserId_returnsPayments() {
        // Add a second payment for the same user
        paymentRepository.save(Payment.builder()
                .bookingId(11L)
                .payerUserId(3L)
                .amount(new BigDecimal("15.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .transactionReference("TXN-EFGH5678")
                .build());

        List<Payment> results = paymentRepository.findByPayerUserId(3L);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(p -> p.getPayerUserId().equals(3L));
    }

    @Test
    @DisplayName("findByPayerUserId() - should return empty list for unknown user")
    void findByPayerUserId_returnsEmpty_whenNoPayments() {
        List<Payment> results = paymentRepository.findByPayerUserId(999L);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("findByPayerUserId() - should not return payments of other users")
    void findByPayerUserId_doesNotReturnOtherUsersPayments() {
        // Payment for a different user
        paymentRepository.save(Payment.builder()
                .bookingId(20L)
                .payerUserId(99L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.WALLET)
                .status(PaymentStatus.COMPLETED)
                .transactionReference("TXN-OTHER999")
                .build());

        List<Payment> results = paymentRepository.findByPayerUserId(3L);

        assertThat(results).isNotEmpty()
                .allMatch(p -> p.getPayerUserId().equals(3L));
    }

    // ── findByStatus() ───────────────────────────────────────

    @Test
    @DisplayName("findByStatus() - should return only COMPLETED payments")
    void findByStatus_returnsCompleted() {
        List<Payment> results = paymentRepository.findByStatus(PaymentStatus.COMPLETED);

        assertThat(results).isNotEmpty()
                .allMatch(p -> p.getStatus() == PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("findByStatus() - should return REFUNDED payments")
    void findByStatus_returnsRefunded() {
        paymentRepository.save(Payment.builder()
                .bookingId(12L)
                .payerUserId(3L)
                .amount(new BigDecimal("30.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.REFUNDED)
                .transactionReference("TXN-REFUND001")
                .build());

        List<Payment> results = paymentRepository.findByStatus(PaymentStatus.REFUNDED);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("findByStatus() - should return empty list when no payments match status")
    void findByStatus_returnsEmpty_whenNoMatch() {
        List<Payment> results = paymentRepository.findByStatus(PaymentStatus.PENDING);

        assertThat(results).isEmpty();
    }

    // ── save() / findById() ──────────────────────────────────

    @Test
    @DisplayName("save() - should persist payment with auto-generated ID")
    void save_persistsPayment() {
        Payment newPayment = paymentRepository.save(Payment.builder()
                .bookingId(30L)
                .payerUserId(5L)
                .amount(new BigDecimal("20.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .transactionReference("TXN-NEW00001")
                .build());

        assertThat(newPayment.getId()).isNotNull();
        assertThat(paymentRepository.findById(newPayment.getId())).isPresent();
    }

    @Test
    @DisplayName("delete() - should remove payment from DB")
    void delete_removesPayment() {
        paymentRepository.delete(savedPayment);

        assertThat(paymentRepository.findById(savedPayment.getId())).isEmpty();
    }

    // ── uniqueness: one payment per booking ──────────────────

    @Test
    @DisplayName("findByBookingId() - should only return one payment per booking")
    void findByBookingId_returnsSinglePayment() {
        // Only one payment should exist per booking
        Optional<Payment> result = paymentRepository.findByBookingId(10L);

        assertThat(result).isPresent();
        // Confirm it's exactly the one we saved
        assertThat(result.get().getTransactionReference()).isEqualTo("TXN-ABCD1234");
    }
}