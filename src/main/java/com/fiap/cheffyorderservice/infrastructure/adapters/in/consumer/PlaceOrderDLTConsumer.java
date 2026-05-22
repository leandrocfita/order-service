package com.fiap.cheffyorderservice.infrastructure.adapters.in.consumer;

import com.fiap.cheffyorderservice.application.ports.in.CancelOrderInputPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class PlaceOrderDLTConsumer {
    private final CancelOrderInputPort cancelOrderInputPort;

    @KafkaListener(
            topics = "order-service-topic-dlt",
            groupId = "order-service-dlt-group"
    )
    public void consumeDlt(String orderId) {

        log.info("Cancelando pedido com id: {}", orderId);

        cancelOrderInputPort.cancelOrder(orderId);

        log.info("Pedido de id: {} cancelado", orderId);
    }
}
