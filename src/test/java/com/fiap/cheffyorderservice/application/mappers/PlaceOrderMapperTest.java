package com.fiap.cheffyorderservice.application.mappers;

import com.fiap.cheffyorderservice.application.mappers.PlaceOrderMapperImpl;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceOrderMapperTest {

    private final PlaceOrderMapper mapper = new PlaceOrderMapperImpl();

    @Test
    void shouldMapOrderToPlaceOrderOutputRecord() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.create(orderId, BigDecimal.valueOf(150));

        PlaceOrderOutputRecord result = mapper.toPlaceOrderOutput(order);

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    void shouldReturnNullWhenOrderIsNull() {
        assertThat(mapper.toPlaceOrderOutput(null)).isNull();
    }
}
