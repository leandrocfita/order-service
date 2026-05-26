package com.fiap.cheffyorderservice.infrastructure.adapters.out.payment;

import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.payment.client.PaymentClient;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAdapterTest {

    @Mock private PaymentClient paymentClient;

    @InjectMocks
    private PaymentAdapter paymentAdapter;

    @Test
    void shouldRequestPaymentSuccessfully() {
        PaymentRequestRecord request = new PaymentRequestRecord(100, "order-id", "client-id");
        paymentAdapter.requestPayment(request);

        verify(paymentClient).requestPayment(request);
    }

    @Test
    void fallbackShouldThrowPaymentServiceExceptionWith503() {
        PaymentRequestRecord request = new PaymentRequestRecord(100, "order-id", "client-id");
        RuntimeException cause = new RuntimeException("retries exhausted");

        assertThatThrownBy(() -> paymentAdapter.requestPaymentFallback(request, cause))
            .isInstanceOf(PaymentServiceException.class)
            .satisfies(e -> assertThat(((PaymentServiceException) e).getHttpStatus()).isEqualTo(503));
    }

    @Test
    void shouldReturnPaymentStatusSuccessfully() {
        String paymentId = "payment-123";
        PaymentStatusResponseRecord responseRecord = new PaymentStatusResponseRecord(paymentId, PaymentStatus.PAID);
        when(paymentClient.getPaymentStatus(paymentId)).thenReturn(ResponseEntity.ok(responseRecord));

        PaymentStatusResponseRecord result = paymentAdapter.getPaymentStatus(paymentId);

        assertThat(result.paymentId()).isEqualTo(paymentId);
        assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldThrowPaymentTimeoutExceptionOnFeignTimeout() {
        String paymentId = "payment-123";
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(408);
        when(paymentClient.getPaymentStatus(paymentId)).thenThrow(feignException);

        assertThatThrownBy(() -> paymentAdapter.getPaymentStatus(paymentId))
            .isInstanceOf(PaymentTimeoutException.class);
    }

    @Test
    void shouldThrowPaymentServiceExceptionOnOtherFeignError() {
        String paymentId = "payment-123";
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);
        when(paymentClient.getPaymentStatus(paymentId)).thenThrow(feignException);

        assertThatThrownBy(() -> paymentAdapter.getPaymentStatus(paymentId))
            .isInstanceOf(PaymentServiceException.class)
            .satisfies(e -> assertThat(((PaymentServiceException) e).getHttpStatus()).isEqualTo(500));
    }
}
