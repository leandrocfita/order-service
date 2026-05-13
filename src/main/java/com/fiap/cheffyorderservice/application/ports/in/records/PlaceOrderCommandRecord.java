package com.fiap.cheffyorderservice.application.ports.in.records;

import java.util.UUID;

public record PlaceOrderCommandRecord(
        UUID orderId,
        Double totalAmount
) {
}
