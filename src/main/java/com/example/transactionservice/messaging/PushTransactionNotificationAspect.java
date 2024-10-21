package com.example.transactionservice.messaging;

import com.example.transactionservice.model.TransactionDto;
import com.example.transactionservice.utils.GenericJsonSerializer;
import lombok.AllArgsConstructor;
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

            System.out.println("========= Pushed Notification with id: " + transaction.id()  + " for user: "
                    + transaction.userId() +  " to topic: " + outputTopic + " =========");
        }
    }
}

