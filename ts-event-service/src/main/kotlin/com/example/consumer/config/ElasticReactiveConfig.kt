package com.example.consumer.config

import com.example.consumer.repository.ElasticClickEventCrudRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.reactive.DefaultReactiveElasticsearchClient
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories

@Configuration
@EnableReactiveElasticsearchRepositories(repositoryBaseClass = ElasticClickEventCrudRepository::class)
class ElasticReactiveConfig {
    @Bean
    fun reactiveElasticsearchTemplate(): ReactiveElasticsearchTemplate {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("localhost:9200")
            .build()
        return ReactiveElasticsearchTemplate(DefaultReactiveElasticsearchClient.create(clientConfiguration))
    }
}