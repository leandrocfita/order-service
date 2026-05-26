package com.fiap.cheffyorderservice.infrastructure.adapters.out.producers;

import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer producer;

    @Test
    void shouldPublishStatusChangeEventToCorrectTopic() {
        UUID orderId = UUID.randomUUID();
        OrderStatusOutputRecord event = new OrderStatusOutputRecord(orderId, PaymentStatus.PAID);

        producer.publishStatusChangeEvent(event);

        verify(kafkaTemplate).send("order.status-change", event);
    }
}
