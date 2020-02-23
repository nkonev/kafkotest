# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/maven-plugin/)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/reference/htmlsingle/#boot-features-kafka)

# Playing with kafka
```bash
docker-compose logs broker | grep retention

docker-compose up -d
docker-compose exec broker bash

kafka-console-consumer --bootstrap-server=broker:29092   --topic advice-topic --offset=earliest --partition=0
kafka-topics --zookeeper zookeeper:2181 --delete --topic advice-topic
kafka-topics  --bootstrap-server=broker:29092  --list
kafka-topics --bootstrap-server=broker:29092  --describe --topic advice-topic

# Get retention
## possible see overrides https://stackoverflow.com/a/42399549/4655234
kafka-topics --bootstrap-server=broker:29092 --describe --topics-with-overrides
kafka-configs --zookeeper zookeeper:2181  --describe --entity-type topics
kafka-configs --zookeeper zookeeper:2181  --describe --entity-type brokers

kafka-console-consumer --bootstrap-server broker:29092 --topic advice-topic --max-messages 2 --offset 0 --partition 0
# step by two message with commit offset
kafka-console-consumer --bootstrap-server broker:9092 --topic advice-topic --max-messages 2 --from-beginning --consumer-property group.id=ololoNikita

exit

# https://docs.confluent.io/current/kafka-rest/quickstart.html
# https://docs.confluent.io/current/kafka-rest/api.html
docker-compose exec rest-proxy bash

curl -v -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
      --data '{"name": "advice_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
      http://localhost:8082/consumers/advice_consumer

curl -v -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["advice-topic"]}' \
 http://localhost:8082/consumers/advice_consumer/instances/advice_consumer_instance/subscription

curl -v -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
      http://localhost:8082/consumers/advice_consumer/instances/advice_consumer_instance/records

# get message without subscription
# https://docs.confluent.io/current/kafka-rest/api.html#get--topics-(string-topic_name)-partitions-(int-partition_id)-messages?offset=(int)[&count=(int)]
# for this works you should set KAFKA_REST_ZOOKEEPER_CONNECT: 'zookeeper:2181' in docker-compose
curl -v -H "Accept: application/vnd.kafka.json.v1+json" 'http://localhost:8082/topics/advice-topic/partitions/0/messages?offset=0'
```
![](.markdown/read_from_rest.png)

# Useful links
* https://www.cloudkarafka.com/blog/2019-04-10-apache-kafka-idempotent-producer-avoiding-message-duplication.html
* https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support/
* https://kafka.apache.org/documentation/#basic_ops_increase_replication_factor
* https://docs.spring.io/spring-kafka/reference/html/#transactions
* https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#common-application-properties
* https://www.confluent.io/blog/transactions-apache-kafka/
* https://cloud.spring.io/spring-cloud-stream-binder-kafka/spring-cloud-stream-binder-kafka.html#_configuration_options
* https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#spring-cloud-stream-overview-binders
* https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.0.RC1/reference/html/spring-cloud-stream-binder-kafka.html#_consuming_batches

KafkaTransactionManager created in KafkaAutoConfiguration @ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")