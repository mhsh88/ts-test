package com.example.producer

import com.example.dto.ClickEvent
import com.example.dto.ImpressionEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, ports = [12345], topics = ["event-topic"])
@AutoConfigureWebTestClient
internal class EventPublisherTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var testConsumer: TestConsumer


    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun receiveImpressionEvent() {

        val event = ImpressionEvent()
        assertThat(event.requestId).isNotNull.isNotEmpty.isNotBlank
        webTestClient.post()
            .uri("/event/impression")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(event), ImpressionEvent::class.java)
            .exchange()
            .expectStatus().isOk

        val objectMapper = ObjectMapper()
        await()
            .atLeast(100, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertEquals(objectMapper.writeValueAsString(event), testConsumer.getMessage())
                assertThat(testConsumer.getMessage()).contains(event.requestId)
            }
    }

    @Test
    fun receiveClickEvent() {

        val event = ClickEvent()
        event.timestamp = Date().time
        val requestId = UUID.randomUUID().toString()
        event.requestId = requestId
        webTestClient.post()
            .uri("/event/click")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(event), ClickEvent::class.java)
            .exchange()
            .expectStatus().isOk

        val objectMapper = ObjectMapper()
        await()
            .atLeast(100, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertEquals(objectMapper.writeValueAsString(event), testConsumer.getMessage())
                assertThat(testConsumer.getMessage()).contains(requestId)
            }
    }
}