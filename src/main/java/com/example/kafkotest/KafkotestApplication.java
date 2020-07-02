package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.*;
import org.springframework.messaging.handler.annotation.Payload;

import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
@SpringBootApplication
@RestController
public class KafkotestApplication {

	private static final Logger logger = LoggerFactory.getLogger(KafkotestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KafkotestApplication.class, args);
	}

	@Value("${tpd.topic-name}")
	private String topicName;

	@Autowired
	private KafkaTemplate<String, Object> template;

	private final int messagesCount = 10;

	@KafkaListener(groupId = "consumer-group", topicPartitions = {@TopicPartition(topic = "${tpd.topic-name}", partitions={"0", "1"})})
	public void listenAsObject(ConsumerRecord<String, PracticalAdvice> cr, @Payload PracticalAdvice payload) {
		logger.info("received: key {}: | Payload: {} | Record: {}", cr.key(), payload, cr.toString());
	}

	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 2, (short) 1);
	}

	@PostMapping("/send")
	public void f() {
		IntStream.range(0, messagesCount).forEach(i -> this.template.send(topicName, String.valueOf(i),
				new PracticalAdvice("A Practical Advice Number " + i, i, LocalDateTime.now())));
		logger.info("All messages sent");
	}
}
