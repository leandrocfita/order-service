package com.fiap.cheffyorderservice.infrastructure.adapters.out.producers;

import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReprocessPaymentEventProducerTest {

    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ReprocessPaymentEventProducer producer;

    @Test
    void shouldPublishReprocessEventToCorrectTopic() {
        UUID orderId = UUID.randomUUID();
        ReprocessOrderOutputRecord event = new ReprocessOrderOutputRecord(orderId, PaymentStatus.PENDING, BigDecimal.TEN, 1);

        producer.publishReprocessOrderEvent(event);

        verify(kafkaTemplate).send("order.pending-payment", event);
    }
}
