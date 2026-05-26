package com.fiap.cheffyorderservice.application.ports.in.records;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderCommandRecord(
        UUID orderId,
        BigDecimal totalAmount
) {
}
