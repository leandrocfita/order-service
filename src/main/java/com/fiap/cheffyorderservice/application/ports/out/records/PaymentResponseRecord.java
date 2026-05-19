package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

public record PaymentResponseRecord(
        PaymentStatus status
) {
}
