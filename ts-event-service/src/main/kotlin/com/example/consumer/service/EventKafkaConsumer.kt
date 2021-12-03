package com.example.consumer.service

import com.example.consumer.document.ElasticClickEvent
import com.example.consumer.document.ImpressionDocument
import com.example.consumer.repository.ElasticClickEventCrudRepository
import com.example.consumer.repository.ImpressionEventCrudRepository
import com.example.dto.BaseEvent
import com.example.dto.ClickEvent
import com.example.dto.EventType
import com.example.dto.ImpressionEvent
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class EventKafkaConsumer(
    private val impressionEventCrudRepository: ImpressionEventCrudRepository,
    private val elasticClickEventCrudRepository: ElasticClickEventCrudRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)


    fun saveImpressionEvent(event: ImpressionEvent) {
        val entity = ImpressionDocument()
        entity.adId = event.adId
        entity.adTitle = event.adTitle
        entity.advertiserCost = event.advertiserCost
        entity.appId = event.appId
        entity.appTitle = event.appTitle
        entity.timestamp = event.timestamp
        entity.requestId = event.requestId
        impressionEventCrudRepository.save(entity)
            .subscribe { s: ImpressionDocument -> log.info("document saved {}", s.id) }
        log.info("impression event consumed adid[{}] adtitle[{}]", event.adId, event.adTitle)
    }

    private fun saveClickEventInElastic(clickEvent: ClickEvent) {
        val byRequestId = impressionEventCrudRepository.findByRequestId(clickEvent.requestId)
        byRequestId!!.subscribe { impressionDocument: ImpressionDocument? ->
            val entity = ElasticClickEvent()
            entity.clickTime = clickEvent.timestamp
            entity.adId = impressionDocument!!.adId
            entity.requestId = impressionDocument.requestId
            entity.adTitle = impressionDocument.adTitle
            entity.impressionTime = impressionDocument.timestamp
            entity.advertiserCost = impressionDocument.advertiserCost
            entity.appId = impressionDocument.appId
            entity.appTitle = impressionDocument.appTitle
            elasticClickEventCrudRepository.save(entity)
                .subscribe { s: ElasticClickEvent -> log.info("saved into elastic {}", s.id) }
        }
        log.info(
            "click event consumed requestId [{}] time stamp[{}]",
            clickEvent.requestId,
            clickEvent.timestamp
        )
    }

    @Bean
    fun eventConsumer(): Consumer<KStream<String?, BaseEvent>> {
        return Consumer { kafkaEventStream: KStream<String?, BaseEvent> ->
            kafkaEventStream.foreach { key: String?, event: BaseEvent ->
                when (event.type) {
                    EventType.ClickEvent -> {
                        saveClickEventInElastic(event as ClickEvent)
                    }
                    EventType.ImpressionEvent -> {
                        saveImpressionEvent(event as ImpressionEvent)
                    }
                    else -> {
                        log.info("unknown event consumed {}", event)
                    }
                }
            }
        }
    }
}