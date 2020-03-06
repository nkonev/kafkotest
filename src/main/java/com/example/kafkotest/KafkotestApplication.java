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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

// https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
@SpringBootApplication
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
	private JdbcTemplate jdbcTemplate;

	private final int messagesCount = 1_000_000;

	@Transactional
	@KafkaListener(topics = "${tpd.topic-name}", clientIdPrefix = "json")
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
			//@Payload PracticalAdvice payload
	) {
		for (PracticalAdvice payload: payloads) {
			Long integer = payload.getIdentifier();
			if (integer%10000 == 0 || integer == messagesCount-1) {
				logger.info("received:  Payload: {}", payload);
			}
			//mongoTemplate.insert(payload);
		}
		//practicalAdviceJdbcRepository.saveAll(payloads);
		jdbcTemplate.batchUpdate(
				"insert into \"practical-advice\" (identifier, message, datetime) values(?,?,?)",
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setLong(1, payloads.get(i).getIdentifier());
						ps.setString(2, payloads.get(i).getMessage());
						ps.setObject(3, payloads.get(i).getDatetime());
					}

					public int getBatchSize() {
						return payloads.size();
					}

				});
		// jdbcTemplate.insert(payloads, PracticalAdvice.class);
	}

	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 1, (short) 3);
	}

	@Override
	@Transactional
	public void run(String... args) {
		IntStream.range(0, messagesCount).forEach(i -> {
			this.template.send(topicName, String.valueOf(i), new PracticalAdvice((long)i, "A Practical Advice Number " + i, LocalDateTime.now()));
		});
		logger.info("All messages sent");
	}

}
