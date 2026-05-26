package com.fiap.cheffyorderservice.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNotFoundExceptionTest {

    @Test
    void shouldStoreMessage() {
        var ex = new OrderNotFoundException("order 123 not found");

        assertThat(ex.getMessage()).isEqualTo("order 123 not found");
    }
}
