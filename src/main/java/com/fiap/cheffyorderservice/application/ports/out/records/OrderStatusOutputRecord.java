package com.fiap.cheffyorderservice.application.ports.out.records;

import java.util.UUID;

public record OrderStatusOutputRecord(
        UUID orderId,
        String status
) {
}
