package com.fiap.cheffyorderservice.domain.model;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {
    private final UUID orderId;
    private final BigDecimal totalAmount;
    private PaymentStatus status;
    private int processingAttempts;

    private Order(
            UUID orderId,
            BigDecimal totalAmount,
            PaymentStatus status,
            int processingAttempts
    ) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.processingAttempts = processingAttempts;
    }

    public static Order restore(UUID orderId, BigDecimal totalAmount, PaymentStatus status, int processingAttempts) {
        return new Order(orderId, totalAmount, status, processingAttempts);
    }

    public static Order create(UUID orderId, BigDecimal totalAmount) {
        return new Order(orderId, totalAmount, PaymentStatus.CREATED, 1);
    }

    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public void incrementProcessingAttempts() {
        this.processingAttempts = this.processingAttempts + 1;
    }

    public int getProcessingAttempts() {
        return this.processingAttempts;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
