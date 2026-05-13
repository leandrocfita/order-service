package com.fiap.cheffyorderservice.domain.model;

import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID orderId;
    private final Double totalAmount;
    private final String status;

    private Order(
            UUID id,
            UUID orderId,
            Double totalAmount,
            String status
    ) {
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public static Order create(UUID orderId, Double totalAmount, String status) {
        return new Order(UUID.randomUUID(), orderId, totalAmount, status);

    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
