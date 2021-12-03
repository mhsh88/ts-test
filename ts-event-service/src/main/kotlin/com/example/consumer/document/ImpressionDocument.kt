package com.example.consumer.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
class ImpressionDocument {
    @Id
    var id: String = UUID.randomUUID().toString()
    var requestId: String = UUID.randomUUID().toString()
    var timestamp: Long = 0
    var adId: String = ""
    var adTitle: String = ""
    var advertiserCost: Double = 0.0
    var appId: String = ""
    var appTitle: String = ""
}