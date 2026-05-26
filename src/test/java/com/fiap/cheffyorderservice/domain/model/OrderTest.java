package com.fiap.cheffyorderservice.domain.model;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void createShouldInitialiseOrderWithCreatedStatusAndOneAttempt() {
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(150);

        Order order = Order.create(orderId, amount);

        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getTotalAmount()).isEqualTo(amount);
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(order.getProcessingAttempts()).isEqualTo(1);
    }

    @Test
    void restoreShouldPreserveAllFields() {
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(200);

        Order order = Order.restore(orderId, amount, PaymentStatus.PAID, 3);

        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getTotalAmount()).isEqualTo(amount);
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getProcessingAttempts()).isEqualTo(3);
    }

    @Test
    void updateStatusShouldChangeStatus() {
        Order order = Order.create(UUID.randomUUID(), BigDecimal.TEN);

        order.updateStatus(PaymentStatus.PAID);

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void updateStatusWithNullShouldNotChangeStatus() {
        Order order = Order.create(UUID.randomUUID(), BigDecimal.TEN);

        order.updateStatus(null);

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    void incrementProcessingAttemptsShouldAddOne() {
        Order order = Order.create(UUID.randomUUID(), BigDecimal.TEN);

        order.incrementProcessingAttempts();

        assertThat(order.getProcessingAttempts()).isEqualTo(2);
    }
}
