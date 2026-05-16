package com.fiap.cheffyorderservice.domain.model;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID orderId;
    private final BigDecimal totalAmount;
    private PaymentStatus status;

    private Order(
            UUID id,
            UUID orderId,
            BigDecimal totalAmount,
            PaymentStatus status
    ) {
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public static Order create(UUID orderId, BigDecimal totalAmount, PaymentStatus status) {
        return new Order(UUID.randomUUID(), orderId, totalAmount, status);
    }

    public static Order create(UUID orderId, BigDecimal totalAmount) {
        return new Order(UUID.randomUUID(), orderId, totalAmount, PaymentStatus.PENDING);
    }

    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public UUID getId() {
        return id;
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
