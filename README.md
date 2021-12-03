# Spring Kafka Streams using Spring Cloud Streams End to End example

## Description

- This is test project for create and producing events using Kafka Streams and Spring cloud Streams in kotlin. Events
  are created using spring reactive web client. Event producer gets these events and send them to Kafka through defined
  topic. At last Consumer read these events from topic and saved them to Mongo and Elasticsearch.

## Requirements

- MongoDb
- ElasticSearch (sudo docker run -d --name es762 -p 9200:9200 -e "discovery.type=single-node" elasticsearch:7.6.2)
- Kafka (https://github.com/confluentinc/cp-all-in-one/tree/7.0.0-post/cp-all-in-one)

## Modules

- 'core' - has common objects that use in another modules
- 'ts-event-generator' - events generated inside this module
- 'ts-event-producer' - events got by this module and sent to kafka
- 'ts-event-service' - events consumed and save through this module

## Microservices

- 'ts-event-generator' - uses reactive web to generate events
- 'ts-event-producer' - uses Spring Kafka
- 'ts-event-service' - uses Spring Cloud Stream with Kafka Streams binders