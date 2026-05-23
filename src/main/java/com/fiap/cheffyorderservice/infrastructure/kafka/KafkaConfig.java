package com.fiap.cheffyorderservice.infrastructure.kafka;

import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import lombok.RequiredArgsConstructor;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaRetryTopic
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean
    public ConsumerFactory<String, InputOrderRecord> jsonConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

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
    public ProducerFactory<String, Object> jsonProducerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InputOrderRecord>
    jsonKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InputOrderRecord> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(jsonConsumerFactory());

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



}
