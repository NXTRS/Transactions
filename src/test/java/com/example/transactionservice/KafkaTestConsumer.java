package com.example.transactionservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaTestConsumer {

    @KafkaListener(topics = "${spring.kafka.topic}")
    public void listener(ConsumerRecord<String, String> consumerRecord) {
    }
}
