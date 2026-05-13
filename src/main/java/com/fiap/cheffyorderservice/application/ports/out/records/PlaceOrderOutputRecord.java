package com.fiap.cheffyorderservice.application.ports.out.records;

import java.util.UUID;

public record PlaceOrderOutputRecord(
        UUID orderId,
        String status
) {
}
