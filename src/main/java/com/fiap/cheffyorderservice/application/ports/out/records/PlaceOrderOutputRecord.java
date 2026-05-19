package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

import java.util.UUID;

public record PlaceOrderOutputRecord(
        UUID orderId,
        PaymentStatus status
) {
}
