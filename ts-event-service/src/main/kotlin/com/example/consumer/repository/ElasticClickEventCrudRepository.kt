package com.example.consumer.repository

import com.example.consumer.document.ElasticClickEvent
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ElasticClickEventCrudRepository : ReactiveCrudRepository<ElasticClickEvent?, String?> {
    fun findByRequestId(id: String?): Mono<ElasticClickEvent?>?
}