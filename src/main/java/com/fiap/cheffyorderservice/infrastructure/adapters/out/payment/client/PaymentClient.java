package com.fiap.cheffyorderservice.infrastructure.adapters.out.payment.client;

import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentResponseRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-client", url = "${payment.api.url}")
public interface PaymentClient {

    @PostMapping(value = "/requisicao", consumes = "application/json")
    ResponseEntity<PaymentResponseRecord> requestPayment(@RequestBody PaymentRequestRecord request);

    @GetMapping(value = "/requisicao/{pagamento_id}", produces = "application/json")
    ResponseEntity<PaymentStatusResponseRecord> getPaymentStatus(@PathVariable("pagamento_id") String pagamentoId);
}
