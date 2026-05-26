package com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers;

import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapperImpl;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InputOrderMapperTest {

    private final InputOrderMapper mapper = new InputOrderMapperImpl();

    @Test
    void shouldMapInputOrderRecordToCommand() {
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(250);
        InputOrderRecord input = new InputOrderRecord(orderId, amount);

        PlaceOrderCommandRecord command = mapper.toCommand(input);

        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.totalAmount()).isEqualTo(amount);
    }

    @Test
    void shouldMapReprocessOrderOutputRecordToCommand() {
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(300);
        ReprocessOrderOutputRecord reprocess = new ReprocessOrderOutputRecord(orderId, PaymentStatus.PENDING, amount, 1);

        PlaceOrderCommandRecord command = mapper.fromReprocessOrder(reprocess);

        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.totalAmount()).isEqualTo(amount);
    }
}
