package com.example.dto

import java.util.*

class ImpressionEvent : BaseEvent() {
    var requestId: String = UUID.randomUUID().toString()
    var timestamp: Long = 0
    var adId: String = ""
    var adTitle: String = ""
    var advertiserCost: Double = 0.0
    var appId: String = ""
    var appTitle: String = ""
    override var type: EventType
        get() = EventType.ImpressionEvent
        set(value) {}

}