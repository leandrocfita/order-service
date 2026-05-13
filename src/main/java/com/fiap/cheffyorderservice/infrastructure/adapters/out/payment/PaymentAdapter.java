package com.fiap.cheffyorderservice.infrastructure.adapters.out.payment;

import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.payment.client.PaymentClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentOutputPort {

    private final PaymentClient paymentClient;

    @Override
    public void requestPayment(PaymentRequestRecord request) {
        try {
            log.info("PaymentAdapter.requestPayment - START - Requesting payment for orderId: [{}], amount: [{}]",
                    request.paymentId(), request.value()
            );

            paymentClient.requestPayment(request);

            log.info("PaymentAdapter.requestPayment - END - Payment successful - orderId: [{}]", request.paymentId());
        } catch (FeignException e) {
            if (e.status() == HttpStatus.REQUEST_TIMEOUT.value()) {
                log.error("PaymentAdapter.requestPayment - ERROR - Timeout on communication with payment API - orderId: [{}]",
                        request.paymentId(), e
                );

                throw new PaymentTimeoutException("Timeout when communicating with external payment API", e);
            }

            log.error("PaymentAdapter.requestPayment - ERROR - Error on payment API - orderId: [{}]",
                    request.paymentId(), e
            );

            throw new PaymentServiceException("Error while processing payment", e.status(), e);
        } catch (Exception e) {
            log.error("PaymentAdapter.requestPayment - ERROR - Unexpected error - orderId: [{}]",
                    request.paymentId(), e
            );

            throw new RuntimeException("Unexpected error when requesting payment", e);
        }
    }

    @Override
    public PaymentStatusResponseRecord getPaymentStatus(String paymentId) {
        try {
            log.info("PaymentAdapter.getPaymentStatus - START - Checking payment status - orderId: [{}]", paymentId);

            ResponseEntity<PaymentStatusResponseRecord> response = paymentClient.getPaymentStatus(paymentId);

            log.info("PaymentAdapter.getPaymentStatus - END - Payment status retrieved successfully - orderId: [{}]", paymentId);
            return response.getBody();
        } catch (FeignException e) {
            if (e.status() == HttpStatus.REQUEST_TIMEOUT.value()) {
                log.error("PaymentAdapter.getPaymentStatus - ERROR - Timeout on communication with payment API - orderId: [{}]",
                        paymentId, e
                );

                throw new PaymentTimeoutException("Timeout when communicating with external payment API", e);
            }

            log.error("PaymentAdapter.getPaymentStatus - ERROR - Error on payment API - orderId: [{}]",
                    paymentId, e
            );

            throw new PaymentServiceException("Error while checking payment status", e.status(), e);
        }
    }
}