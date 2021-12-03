package com.example.consumer.repository

import com.example.consumer.document.ImpressionDocument
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ImpressionEventCrudRepository : ReactiveCrudRepository<ImpressionDocument?, String?> {
    fun findByRequestId(id: String?): Mono<ImpressionDocument?>?
}