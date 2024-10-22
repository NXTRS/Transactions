package com.example.transactionservice.messaging;

import com.example.transactionservice.model.TransactionDto;
import com.example.transactionservice.utils.GenericJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class PushTransactionNotificationAspect {

    @Value("${app.transaction-limit}")
    private final Double transactionLimit;
    @Value("${spring.kafka.topic}")
    private final String outputTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final GenericJsonSerializer jsonSerializer;

    @Pointcut("@annotation(PushTransactionNotification)")
    public void pushTransactionNotificationPointcut() {}

    @AfterReturning(pointcut = "pushTransactionNotificationPointcut()", returning = "result")
    public void checkAndPushNotification(Object result) {
        var transaction = (TransactionDto) result;
        if (transaction.amount() > transactionLimit) {
            kafkaTemplate.send(new ProducerRecord<>(
                    outputTopic,
                    transaction.userId(),
                    jsonSerializer.serialize(transaction)));

            log.info("Pushed transaction event with id: {} for user: {} on topic {}",
                    transaction.id(), transaction.userId(), outputTopic);
        }
    }
}

