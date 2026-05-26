package com.fiap.cheffyorderservice.infrastructure.adapters.in.consumer;

import com.fiap.cheffyorderservice.application.ports.in.CancelOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReprocessOrderConsumerTest {

    @Mock private CancelOrderInputPort cancelOrderInputPort;
    @Mock private PlaceOrderInputPort placeOrderInputPort;
    @Mock private InputOrderMapper inputOrderMapper;

    @InjectMocks
    private ReprocessOrderConsumer reprocessOrderConsumer;

    @Test
    void shouldOnlyExecutePlaceOrderWhenBelowMaxAttempts() {
        UUID orderId = UUID.randomUUID();
        ReprocessOrderOutputRecord order = new ReprocessOrderOutputRecord(orderId, PaymentStatus.PENDING, BigDecimal.TEN, 1);
        PlaceOrderCommandRecord command = new PlaceOrderCommandRecord(orderId, BigDecimal.TEN);

        when(inputOrderMapper.fromReprocessOrder(order)).thenReturn(command);
        when(placeOrderInputPort.execute(command))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PENDING));

        reprocessOrderConsumer.reprocessOrder(order);

        verify(cancelOrderInputPort, never()).cancelOrder(any());
        verify(placeOrderInputPort).execute(command);
    }

    @Test
    void shouldCancelAndThenExecuteWhenAtMaxAttempts() {
        UUID orderId = UUID.randomUUID();
        ReprocessOrderOutputRecord order = new ReprocessOrderOutputRecord(orderId, PaymentStatus.PENDING, BigDecimal.TEN, 2);
        PlaceOrderCommandRecord command = new PlaceOrderCommandRecord(orderId, BigDecimal.TEN);

        when(inputOrderMapper.fromReprocessOrder(order)).thenReturn(command);
        when(placeOrderInputPort.execute(command))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.CANCELED));

        reprocessOrderConsumer.reprocessOrder(order);

        verify(cancelOrderInputPort).cancelOrder(orderId.toString());
        verify(placeOrderInputPort).execute(command);
    }
}
