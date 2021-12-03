package com.example.consumer

import com.example.consumer.repository.ElasticClickEventCrudRepository
import com.example.consumer.repository.ImpressionEventCrudRepository
import com.example.dto.BaseEvent
import com.example.dto.ClickEvent
import com.example.dto.ImpressionEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.time.Duration
import java.util.*
import java.util.function.Consumer


@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventSubscriberApplicationTests {

    private lateinit var testInputTopic: TestInputTopic<String, BaseEvent>
    private lateinit var testDriver: TopologyTestDriver


    @Autowired
    lateinit var eventConsumer: Consumer<KStream<String?, BaseEvent>>

    @Autowired
    lateinit var impressionEventRepository: ImpressionEventCrudRepository

    @Autowired
    lateinit var clickEventCrudRepository: ElasticClickEventCrudRepository

    @BeforeAll
    fun startUp() {
        val streamsBuilder = StreamsBuilder()
        val string = Serdes.String()
        val jsonDeserializer = JsonDeserializer<BaseEvent>()
        val jsonSerializer = JsonSerializer<BaseEvent>()
        jsonDeserializer.addTrustedPackages("*")
        val event = Serdes.serdeFrom(jsonSerializer, jsonDeserializer)
        val topic = "event-topic"
        val stream = streamsBuilder.stream(topic, Consumed.with(string, event))

        eventConsumer.accept(stream)


        val properties = Properties()
        properties.putAll(
            mapOf(
                Pair(StreamsConfig.APPLICATION_ID_CONFIG, topic),
                Pair(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092"),
                Pair(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, string.javaClass.name),
                Pair(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, event.javaClass.name)
            )
        )

        testDriver = TopologyTestDriver(streamsBuilder.build(), properties)

        testInputTopic = testDriver.createInputTopic(topic, string.serializer(), event.serializer())

    }


    @AfterAll
    fun tearDown() {
        testDriver.close()
    }

    @Test
    fun producedImpressionEvent_MustSavedInMongoDb() {
        val impressionEvent = ImpressionEvent()
        testInputTopic.pipeInput("key1", impressionEvent)
        val document = impressionEventRepository.findByRequestId(impressionEvent.requestId)!!.block()
        assertThat(document).isNotNull
        assertThat(document?.requestId).isNotNull.isEqualTo(impressionEvent.requestId)
        assertThat(document?.adId)
    }

    @Test
    fun producedClickEvent_MustSavedInElastic() {
        val impressionEvent = ImpressionEvent()
        val addId = "add_id"
        impressionEvent.adId = addId
        val appId = "app_id"
        impressionEvent.appId = appId
        val addTitle = "add title"
        impressionEvent.adTitle = addTitle
        val appTitle = "app_title"
        impressionEvent.appTitle = appTitle
        val impressionTime = Date().time
        impressionEvent.timestamp = impressionTime
        testInputTopic.pipeInput("key1", impressionEvent)
        await()
            .atMost(Duration.ofMinutes(1))
            .untilAsserted {
                val document = impressionEventRepository.findByRequestId(impressionEvent.requestId)!!.block()
                assertThat(document).isNotNull
                assertThat(document?.requestId).isNotNull.isEqualTo(impressionEvent.requestId)
            }

        val clickEvent = ClickEvent()
        clickEvent.requestId = impressionEvent.requestId
        val clickTime = Date().time
        clickEvent.timestamp = clickTime
        testInputTopic.pipeInput("key2", clickEvent)

        await()
            .atMost(Duration.ofMinutes(1))
            .untilAsserted {
                val elasticClickEvent = clickEventCrudRepository.findByRequestId(clickEvent.requestId)!!.block()
                assertThat(elasticClickEvent).isNotNull
                assertThat(elasticClickEvent?.requestId).isEqualTo(impressionEvent.requestId)
                assertThat(elasticClickEvent?.clickTime).isEqualTo(clickTime)
                assertThat(elasticClickEvent?.adId).isEqualTo(addId)
                assertThat(elasticClickEvent?.appId).isEqualTo(appId)
                assertThat(elasticClickEvent?.adTitle).isEqualTo(addTitle)
            }
    }


}