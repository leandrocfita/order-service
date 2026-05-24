package com.fiap.cheffyorderservice.infrastructure.adapters.in.consumer;

import com.fiap.cheffyorderservice.application.ports.in.CancelOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ReprocessOrderConsumer {
    private final CancelOrderInputPort cancelOrderInputPort;
    private final PlaceOrderInputPort placeOrderInputPort;
    private final InputOrderMapper inputOrderMapper;

    private final int MAX_ATTEMPTS = 2;

    @KafkaListener(
            topics = "order.pending-payment",
            groupId = "order.pending-payment.group.",
            containerFactory = "reprocessOrderListenerContainerFactory"
    )
    public void reprocessOrder(ReprocessOrderOutputRecord order) {
        String orderId = order.orderId().toString();
        int reprocessAttempt = order.attempt();

        log.info("Reprocessing order - OrderId: {} - Reprocess attempts: {}", orderId, reprocessAttempt);

        if (reprocessAttempt == MAX_ATTEMPTS) {
            log.info("Canceling order for exceeding max reprocess payment attempts - OrderId: {} - Reprocess attempts: {}", orderId, reprocessAttempt);

            cancelOrderInputPort.cancelOrder(orderId);

            log.info("Pedido de id: {} cancelado", orderId);
        }

        placeOrderInputPort.execute(inputOrderMapper.fromReprocessOrder(order));





    }
}
