package com.shopsense.orchestratorservice.config;

import com.shopsense.commonservice.util.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    private static final int PARTITIONS = 3;

    private static final short REPLICATION_FACTOR = 1;

    @Bean
    public NewTopic orderEventsTopic() {
        return createTopic(KafkaTopics.ORDER_EVENTS);
    }

    @Bean
    public NewTopic orderSagaResultsTopic() {
        return createTopic(KafkaTopics.ORDER_SAGA_RESULTS);
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return createTopic(KafkaTopics.INVENTORY_EVENTS);
    }

    @Bean
    public NewTopic inventoryCommandsTopic() {
        return createTopic(KafkaTopics.INVENTORY_COMMANDS);
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return createTopic(KafkaTopics.PAYMENT_EVENTS);
    }

    @Bean
    public NewTopic paymentCommandsTopic() {
        return createTopic(KafkaTopics.PAYMENT_COMMANDS);
    }

    @Bean
    public NewTopic sagaEventsTopic() {
        return createTopic(KafkaTopics.SAGA_EVENTS);
    }

    private NewTopic createTopic( String topic ) {
        return TopicBuilder.name(topic)
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

}
