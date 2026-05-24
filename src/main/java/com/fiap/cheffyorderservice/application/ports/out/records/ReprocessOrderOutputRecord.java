package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ReprocessOrderOutputRecord(
        UUID orderId,
        PaymentStatus status,
        BigDecimal totalAmount,
        int attempt
) {
}
