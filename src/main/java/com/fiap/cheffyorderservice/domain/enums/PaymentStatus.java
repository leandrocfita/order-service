package com.fiap.cheffyorderservice.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    PAID("pago"),
    SENT_TO_PAYMENT_GATEWAY("enviado"),
    PENDING("pendente"),
    CANCELED("canceled");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return status;
    }
}
