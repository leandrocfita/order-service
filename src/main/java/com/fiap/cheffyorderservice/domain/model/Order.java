package com.fiap.cheffyorderservice.domain.model;

import com.fiap.cheffyorderservice.domain.enums.AllowedCurrencies;

import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID orderId;
    private final Double totalAmount;
    private final String currency;
    private final String status;

    private Order(
            UUID id,
            UUID orderId,
            Double totalAmount,
            String currency,
            String status
    ) {
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.currency = validateCurrency(currency);
        this.status = status;
    }

    public static Order create(UUID orderId, Double totalAmount, String currency, String status) {
        return new Order(UUID.randomUUID(), orderId, totalAmount, currency, status);

    }

    private String validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("A moeda não pode ser nula ou vazia");
        }

        if (!AllowedCurrencies.isValid(currency)) {
            throw new IllegalArgumentException("Moeda inválida");
        }

        return currency;
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

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }
}
