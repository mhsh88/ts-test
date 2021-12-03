package com.example.consumer.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.util.*

@Document(indexName = "test")
class ElasticClickEvent {
    @Id
    var id: String = UUID.randomUUID().toString()
    var requestId: String? = null
    var impressionTime: Long? = null
    var clickTime: Long? = null
    var adId: String? = null
    var adTitle: String? = null
    var advertiserCost: Double? = null
    var appId: String? = null
    var appTitle: String? = null
}