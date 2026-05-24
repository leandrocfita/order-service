package com.fiap.cheffyorderservice.infrastructure.kafka;

import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaRetryTopic
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    //CONSUMER FACTORIES
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean
    public ConsumerFactory<String, InputOrderRecord> placeOrderListenerConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<InputOrderRecord> deserializer =
                new JsonDeserializer<>(InputOrderRecord.class);

        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, ReprocessOrderOutputRecord> reprocessConsumerFactory() {

        JsonDeserializer<ReprocessOrderOutputRecord> deserializer =
                new JsonDeserializer<>(ReprocessOrderOutputRecord.class);

        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ProducerFactory<String, Object> jsonProducerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    //LISTENER FACTORIES

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InputOrderRecord>
    placeOrderListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InputOrderRecord> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(placeOrderListenerConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReprocessOrderOutputRecord>
    reprocessOrderListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ReprocessOrderOutputRecord> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(reprocessConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());

        return factory;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    stringKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(stringConsumerFactory());

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(jsonProducerFactory());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        return new DefaultErrorHandler(
                (record, exception) -> {
                    log.error(
                            "Message discarded due to deserialization/processing error. topic={}, partition={}, offset={}, error={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            exception.getMessage(),
                            exception
                    );
                }
        );
    }



}
