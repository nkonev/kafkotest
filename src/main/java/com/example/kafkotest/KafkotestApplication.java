package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

// https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
@SpringBootApplication
public class KafkotestApplication {

	private static final Logger logger = LoggerFactory.getLogger(KafkotestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KafkotestApplication.class, args);
	}

	@Value("${tpd.topic-name}")
	private String topicName;

	@Autowired
	private KafkaTemplate<String, Object> template;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Transactional
	@KafkaListener(topics = "${tpd.topic-name}", clientIdPrefix = "json")
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
			//@Payload PracticalAdvice payload
	) {
		for (PracticalAdvice payload: payloads) {
			logger.info("received:  Payload: {}", payload);
			//mongoTemplate.insert(payload);
		}
		mongoTemplate.insert(payloads, PracticalAdvice.class);
	}

	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 1, (short) 1);
	}


	@PostConstruct
	public void pc() {
		// should not be in transaction
		if (!mongoTemplate.collectionExists(PracticalAdvice.class)) {
			mongoTemplate.createCollection(PracticalAdvice.class);
		}
	}
}
