package com.fiap.cheffyorderservice.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    PAID("pago"),
    ACCEPTED("accepted"),
    PENDING("pendente");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return status;
    }
}
