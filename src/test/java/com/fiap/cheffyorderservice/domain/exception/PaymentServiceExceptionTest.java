package com.fiap.cheffyorderservice.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceExceptionTest {

    @Test
    void shouldStoreMessageAndHttpStatus() {
        var ex = new PaymentServiceException("service unavailable", 503);

        assertThat(ex.getMessage()).isEqualTo("service unavailable");
        assertThat(ex.getHttpStatus()).isEqualTo(503);
    }

    @Test
    void shouldStoreMessageHttpStatusAndCause() {
        var cause = new RuntimeException("root cause");
        var ex = new PaymentServiceException("service error", 500, cause);

        assertThat(ex.getMessage()).isEqualTo("service error");
        assertThat(ex.getHttpStatus()).isEqualTo(500);
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
