package com.fiap.cheffyorderservice.infrastructure.adapters.in.consumer;

import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceOrderConsumer {

    private final PlaceOrderInputPort placeOrderInputPort;
    private final InputOrderMapper inputOrderMapper;

    @RetryableTopic(
            backoff = @Backoff(
                    delay = 5000,
                    multiplier = 2.0
            )
    )
    @KafkaListener(
            topics = "order-service-topic",
            groupId = "order-service-group")
    public void consume(InputOrderRecord message) {

        log.info("Received message: {}", message);

        placeOrderInputPort.execute(inputOrderMapper.toCommand(message));

        log.info("Message processed: {}", message);
    }
}
