package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentResponseRecord(
        @JsonProperty("status")
        String status
) {
}
