package com.fiap.cheffyorderservice.infrastructure.adapters.in.controllers.exception;

import com.fiap.cheffyorderservice.domain.exception.OrderNotFoundException;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn408ForPaymentTimeoutException() {
        var ex = new PaymentTimeoutException("request timed out");

        var response = handler.handlePaymentTimeout(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturnHttpStatusFromPaymentServiceException() {
        var ex = new PaymentServiceException("service unavailable", 503);

        var response = handler.handlePaymentService(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void shouldReturn500WhenPaymentServiceExceptionHasInvalidHttpStatus() {
        var ex = new PaymentServiceException("unknown error", 999);

        var response = handler.handlePaymentService(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturn404ForOrderNotFoundException() {
        var ex = new OrderNotFoundException("order not found");

        var response = handler.handleOrderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }
}
