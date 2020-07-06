package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
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
import org.springframework.kafka.listener.BatchLoggingErrorHandler;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
@SpringBootApplication
@RestController
public class KafkotestApplication implements CommandLineRunner {

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

	private final int messagesCount = 10;

	@KafkaListener(topics = "${tpd.topic-name}")
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
			//@Payload PracticalAdvice payload
	) {
		for (PracticalAdvice payload: payloads) {
			logger.info("received:  Payload: {}", payload);

			payload.setIdentifier(null);
			mongoTemplate.insert(payload);
			//
		}
	}

	@Bean
	public BatchLoggingErrorHandler batchLoggingErrorHandler() {
		return new BatchLoggingErrorHandler();
	}

	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 1, (short) 3);
	}

	private void sendN() {
		IntStream.range(0, messagesCount).forEach(i -> {
			this.template.send(topicName, String.valueOf(i), new PracticalAdvice(String.valueOf(i), "A Practical Advice Number " + i, LocalDateTime.now()));
		});
		logger.info("All messages sent");
	}

	@Override
	public void run(String... args) {
		sendN();
	}

	@PostMapping("/send")
	public void f() {
		sendN();
	}
}
