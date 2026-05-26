package com.fiap.cheffyorderservice.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTimeoutExceptionTest {

    @Test
    void shouldStoreMessage() {
        var ex = new PaymentTimeoutException("timeout");

        assertThat(ex.getMessage()).isEqualTo("timeout");
    }

    @Test
    void shouldStoreMessageAndCause() {
        var cause = new RuntimeException("feign timeout");
        var ex = new PaymentTimeoutException("external timeout", cause);

        assertThat(ex.getMessage()).isEqualTo("external timeout");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
