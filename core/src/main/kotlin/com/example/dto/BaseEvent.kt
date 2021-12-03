package com.example.dto

import java.io.Serializable

abstract class BaseEvent : Serializable {
    abstract var type: EventType

}