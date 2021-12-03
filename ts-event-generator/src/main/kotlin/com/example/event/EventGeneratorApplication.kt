package com.example.event

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventGeneratorApplication

fun main(args: Array<String>) {
    runApplication<EventGeneratorApplication>(*args)
}