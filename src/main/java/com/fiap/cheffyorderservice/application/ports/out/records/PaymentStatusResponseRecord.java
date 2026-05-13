package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentStatusResponseRecord(
        @JsonProperty("pagamento_id") String paymentId,
        String status
) {}
