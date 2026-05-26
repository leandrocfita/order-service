package com.fiap.cheffyorderservice.application.mappers;

import com.fiap.cheffyorderservice.application.mappers.OrderStatusMapperImpl;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusMapperTest {

    private final OrderStatusMapper mapper = new OrderStatusMapperImpl();

    @Test
    void shouldMapPaymentStatusResponseToOrderStatusOutput() {
        UUID orderId = UUID.randomUUID();
        PaymentStatusResponseRecord response = new PaymentStatusResponseRecord(orderId.toString(), PaymentStatus.PAID);

        OrderStatusOutputRecord result = mapper.toOrderStatusOutputRecord(response);

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertThat(mapper.toOrderStatusOutputRecord(null)).isNull();
    }
}
