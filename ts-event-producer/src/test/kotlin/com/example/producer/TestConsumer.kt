package com.example.producer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component


@Component
class TestConsumer {


    private var message: Any? = null

    @KafkaListener(topics = ["event-topic"], groupId = "group.id")
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        message = consumerRecord.value()
    }

    fun getMessage(): String? {
        return message.toString()
    }
}