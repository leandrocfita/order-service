package com.fiap.cheffyorderservice.infrastructure.adapters.in.records;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InputOrderRecord(
        @NotNull
        UUID orderId,

        @NotEmpty
        String currency,

        @NotNull
        @Positive
        Double totalAmount,

        @NotEmpty
        String status
) {
}
