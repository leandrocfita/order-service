package com.fiap.cheffyorderservice.infrastructure.adapters.out.payment;

import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.payment.client.PaymentClient;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    @CircuitBreaker(
            name = "payment",
            fallbackMethod = "requestPaymentFallback"
    )
    @Retry(name = "payment")
    public void requestPayment(PaymentRequestRecord request) {

            log.info("HTTP request sent to payment API to request orderId [orderId={}, amount={}]",
                    request.paymentId(), request.value()
            );

            paymentClient.requestPayment(request);

            log.info("Payment request completed successfully [orderId={}]", request.paymentId());
    }

    @Override
    public PaymentStatusResponseRecord getPaymentStatus(String paymentId) {
        try {
            log.info("HTTP request sent to payment API to check payment status [orderId={}]", paymentId);

            ResponseEntity<PaymentStatusResponseRecord> response = paymentClient.getPaymentStatus(paymentId);

            log.info("Payment status retrieved successfully [orderId={}]", paymentId);
            return response.getBody();
        } catch (FeignException e) {
            if (e.status() == HttpStatus.REQUEST_TIMEOUT.value()) {
                log.error("Timeout while communicating with payment API [orderId={}, statusCode={}, message={}]",
                        paymentId, e.status(), e.getMessage(), e
                );

                throw new PaymentTimeoutException("Timeout when communicating with external payment API", e);
            }

            log.error("Payment API returned an error while checking payment status [paymentId={}, statusCode={}, message={}]",
                    paymentId, e.status(), e.getMessage(), e

            );
            throw new PaymentServiceException("Error while checking payment status", e.status(), e);
        }
    }

    public void requestPaymentFallback(
            PaymentRequestRecord request,
            Throwable ex
    ) {

        log.error(
                "Payment service unavailable after retries [orderId={}]",
                request.paymentId(),
                ex
        );

        throw new PaymentServiceException(
                "Payment service unavailable after retries",
                503,
                ex
        );
    }
}