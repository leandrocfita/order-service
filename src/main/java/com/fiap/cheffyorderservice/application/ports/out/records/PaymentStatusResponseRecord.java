package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;

public record PaymentStatusResponseRecord(
        @JsonProperty("pagamento_id") String paymentId,
        PaymentStatus status
) {}
