package com.example.event

import com.example.dto.ClickEvent
import com.example.dto.ImpressionEvent
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Service
class EventGeneratorService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.create()

    @Value("\${event.click.url}")
    lateinit var clickEventUrl: String

    @Value("\${event.impression.url}")
    lateinit var impressionEventUrl: String

    @Value("\${event.click.random.num}")
    lateinit var clickEventRandomNumber: String

    @Value("\${event.click.delay}")
    lateinit var clickEventDelay: String

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    private fun randomEventGenerator() {
        val requestId = sendImpressionEvent()
        sendClickEvent(requestId)
    }

    private fun sendClickEvent(requestId: String) {
        val random = Math.random()
        if (random > clickEventRandomNumber.toDouble()) {
            val clickEvent = ClickEvent()
            clickEvent.timestamp = Date().time
            clickEvent.requestId = requestId
            webClient
                .post()
                .uri(clickEventUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    Mono.just(clickEvent)
                        .delaySubscription(Duration.ofSeconds(clickEventDelay.toLong())) // delayed click call after . seconds
                    , ClickEvent::class.java
                )
                .retrieve()
                .bodyToMono(String::class.java) //                    .delaySubscription(Duration.ofSeconds(1))
                .subscribe { s: String? ->
                    log.info(
                        "random number: {},  click event send response {}",
                        random,
                        s
                    )
                }
        }
    }

    private fun sendImpressionEvent(): String {
        val impressionEvent = ImpressionEvent()
        impressionEvent.timestamp = Date().time
        val requestId = UUID.randomUUID().toString()
        impressionEvent.requestId = requestId
        impressionEvent.appTitle = getString(10)
        impressionEvent.adId = UUID.randomUUID().toString()
        impressionEvent.advertiserCost = Math.random() * 10000
        impressionEvent.appId = UUID.randomUUID().toString()
        impressionEvent.adTitle = getString(20)
        val impressionResponse = webClient
            .post()
            .uri(impressionEventUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(impressionEvent), ImpressionEvent::class.java)
            .retrieve().bodyToMono(String::class.java)
        impressionResponse.subscribe { s: String? -> log.info("impression event {}", s) }
        return requestId
    }

    private fun getString(length: Int): String {
        val useLetters = true
        val useNumbers = false
        return RandomStringUtils.random(length, useLetters, useNumbers)
    }
}