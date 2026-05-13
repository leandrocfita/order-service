package com.fiap.cheffyorderservice.application.ports.out;

import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;

public interface PaymentOutputPort {
    void requestPayment(PaymentRequestRecord request);
    PaymentStatusResponseRecord getPaymentStatus(String paymentId);
}
