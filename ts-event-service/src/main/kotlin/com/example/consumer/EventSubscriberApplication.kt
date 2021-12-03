package com.example.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.example.consumer.config", "com.example.consumer.service"])
class EventSubscriberApplication

fun main(args: Array<String>) {
    runApplication<EventSubscriberApplication>(*args)
}
