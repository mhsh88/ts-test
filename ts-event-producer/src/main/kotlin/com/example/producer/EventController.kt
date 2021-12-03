package com.example.producer

import com.example.dto.ClickEvent
import com.example.dto.ImpressionEvent
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventController(private val eventService: EventService) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/impression")
    fun receivedImpressionEvent(@RequestBody impressionEvent: ImpressionEvent?): String {
        log.info("impression event received {} ", impressionEvent)
        eventService.registerEvents(impressionEvent!!)
        return "impression event received"
    }

    @PostMapping("/click")
    fun receivedClickEvent(@RequestBody clickEvent: ClickEvent?): String {
        log.info("click event received {} ", clickEvent)
        eventService.registerEvents(clickEvent!!)
        return "click event received"
    }
}