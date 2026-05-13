package com.fiap.cheffyorderservice.application.ports.out.records;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentRequestRecord(
        @JsonProperty("valor") Integer value,
        @JsonProperty("pagamento_id") String paymentId,
        @JsonProperty("cliente_id") String clientId
) {}
