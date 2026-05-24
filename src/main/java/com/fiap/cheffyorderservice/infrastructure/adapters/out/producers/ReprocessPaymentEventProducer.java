package com.fiap.cheffyorderservice.infrastructure.adapters.out.producers;

import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.ReprocessOrderOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ReprocessPaymentEventProducer implements ReprocessOrderOutputPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ORDER_STATUS_CHANGE_TOPIC = "order.pending-payment";


    @Override
    public void publishReprocessOrderEvent(ReprocessOrderOutputRecord order) {
        log.info("Sending order to pending payment topic: {}", order);

        kafkaTemplate.send(ORDER_STATUS_CHANGE_TOPIC, order);

        log.info("Order sent to pending payment topic: {}", order);
    }
}
