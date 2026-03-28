package com.orchestration.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {

    @Value("${spring.application.instance-id:instance-1}")
    private String instanceId;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public NewTopic orchestrationTriggerTopic() {
        return TopicBuilder.name("orchestration-trigger")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orchestrationEventTopic() {
        return TopicBuilder.name("orchestration-event")
                .partitions(6)
                .replicas(1)
                .build();
    }
}
