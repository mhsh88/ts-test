package com.example.dto

class ClickEvent : BaseEvent() {
    var requestId: String = ""
    var timestamp: Long = 0
    override var type: EventType
        get() = EventType.ClickEvent
        set(value) {
        }
}