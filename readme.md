# KSQL
* https://docs.confluent.io/current/ksql/docs/developer-guide/syntax-reference.html
* https://blog.knoldus.com/ksql-streams-and-tables/
* https://blog.knoldus.com/ksql-getting-started-with-streaming-sql-for-apache-kafka/
* https://www.confluent.io/blog/troubleshooting-ksql-part-1/#later-offset
```
docker-compose exec ksql-server ksql
CREATE STREAM advices_original (identifier varchar, message varchar, datetime varchar) WITH  (kafka_topic='advice-topic', value_format='JSON');
DESCRIBE EXTENDED advices_original;
SET 'auto.offset.reset'='earliest';
SELECT * FROM advices_original EMIT CHANGES;
...and wait > 30 seconds

SET 'auto.offset.reset'='earliest';
SELECT * FROM advices_original where identifier='900000' EMIT CHANGES;
...and wait > 30 seconds.
```

http://localhost:8080/actuator/prometheus
```
kafka_consumer_fetch_manager_records_consumed_total
kafka_producer_record_send_total
kafka_producer_topic_record_send_total
```