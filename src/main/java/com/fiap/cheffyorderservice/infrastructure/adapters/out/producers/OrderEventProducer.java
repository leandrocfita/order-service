package com.fiap.cheffyorderservice.infrastructure.adapters.out.producers;

import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class OrderEventProducer implements OrderStatusChangeOutputPort {

    private final KafkaTemplate<String, OrderStatusOutputRecord> kafkaTemplate;
    private static final String ORDER_STATUS_CHANGE_TOPIC = "order.status-change-topic";

    @Override
    public void publishStatusChangeEvent(OrderStatusOutputRecord orderStatusOutputRecord) {

        log.info("Publicando evento de alteração de status do pedido: {}", orderStatusOutputRecord);

        kafkaTemplate.send(ORDER_STATUS_CHANGE_TOPIC, orderStatusOutputRecord);

        log.info("Evento de alteração de status do pedido publicado: {}", orderStatusOutputRecord);

    }
}
