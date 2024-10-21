package com.example.transactionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public KafkaAdmin.NewTopics topic(@Value("${spring.kafka.topic}") String topicName) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(topicName)
                        .build()
        );
    }
}
