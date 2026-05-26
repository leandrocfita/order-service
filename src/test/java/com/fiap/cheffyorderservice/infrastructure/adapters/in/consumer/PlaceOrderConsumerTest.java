package com.fiap.cheffyorderservice.infrastructure.adapters.in.consumer;

import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderConsumerTest {

    @Mock private PlaceOrderInputPort placeOrderInputPort;
    @Mock private InputOrderMapper inputOrderMapper;

    @InjectMocks
    private PlaceOrderConsumer placeOrderConsumer;

    @Test
    void shouldDelegateToPlaceOrderInputPort() {
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        InputOrderRecord message = new InputOrderRecord(orderId, amount);
        PlaceOrderCommandRecord command = new PlaceOrderCommandRecord(orderId, amount);

        when(inputOrderMapper.toCommand(message)).thenReturn(command);
        when(placeOrderInputPort.execute(command))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.CREATED));

        placeOrderConsumer.consume(message);

        verify(inputOrderMapper).toCommand(message);
        verify(placeOrderInputPort).execute(command);
    }
}
