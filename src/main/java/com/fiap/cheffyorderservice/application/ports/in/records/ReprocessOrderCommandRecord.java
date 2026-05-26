package com.fiap.cheffyorderservice.application.ports.in.records;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

import java.util.UUID;

public record ReprocessOrderCommandRecord(
        UUID orderId,
        PaymentStatus status,
        int attempt
) {
}
