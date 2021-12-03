package com.example.producer

import com.example.dto.BaseEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventService(private val kafkaTemplate: KafkaTemplate<String, BaseEvent>) {
    fun registerEvents(event: BaseEvent) {
        kafkaTemplate.send(KAFKA_EVENT_TOPIC, event)
    }

    companion object {
        private const val KAFKA_EVENT_TOPIC = "event-topic"
    }
}