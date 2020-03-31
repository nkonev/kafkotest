package com.example.kafkotest;

import io.tpd.kafkaexample.DocumentDlq;
import io.tpd.kafkaexample.PracticalAdvice;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
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
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;

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
	private MongoTemplate mongoTemplate;

	private final int messagesCount = 30;

	private static final String errorHandlerBeanName = "listen3ErrorHandler";

	@Value("${tpd.no-rollback-for}")
	private String va;

	@Transactional
	@KafkaListener(topics = "${tpd.topic-name}", clientIdPrefix = "json"//, errorHandler = errorHandlerBeanName
			 )
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
	) {
		logger.info("received batch of {}", payloads.size());
		for (PracticalAdvice payload: payloads) {
			logger.info("received:  Payload: {}", payload);
			try {
				mongoTemplate.insert(payload);
			} catch (Exception e) {
				logger.error("Error on payload: {}", payload, e);
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				mongoTemplate.insert(new DocumentDlq(null, e.getClass().getName(), exceptionAsString));
			}
		}
		//mongoTemplate.insert(payloads, PracticalAdvice.class);
	}


	/*@Bean
	BatchErrorHandler batchErrorHandler() {
		return new BatchLoggingErrorHandler() {
			// я должен не только коммитнуть транзакцию
			// но и где-то перемотать оффсет
			@Override
			public boolean isAckAfterHandle() {
				return true;
			}
		};
	}

	@Bean(name = errorHandlerBeanName)
	public ConsumerAwareListenerErrorHandler listen3ErrorHandler() {
		return (m, e, c) -> {
			// возвращая null мы проглатывает эксепшн
			logger.error("Error on payload: {}", m.getPayload(), e);
			MessageHeaders headers = m.getHeaders();
			List<String> topics = headers.get(KafkaHeaders.RECEIVED_TOPIC, List.class);
			List<Integer> partitions = headers.get(KafkaHeaders.RECEIVED_PARTITION_ID, List.class); // ид партиций
			List<Long> offsets = headers.get(KafkaHeaders.OFFSET, List.class); // оффсеты каждого сообщения из батча
			Map<TopicPartition, Long> offsetsToReset = new HashMap<>();
			for (int i = 0; i < topics.size(); i++) {
				int index = i;
				offsetsToReset.compute(
						new TopicPartition(topics.get(i), partitions.get(i)),
						(topicPartition, offset) -> offset == null ? offsets.get(index) : Math.min(offset, offsets.get(index))
				);
			}
			offsetsToReset.forEach((k, v) -> c.seek(k, v+1));
			return null;
		};
	}*/

	/*@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
	kafkaListenerContainerFactory(ConsumerFactory<String, Object> cf
//			, ContainerProperties containerProperties
	) {
		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setBatchListener(true);
		factory.setBatchErrorHandler(new BatchLoggingErrorHandler());
		factory.setConsumerFactory(cf);
//		factory.getContainerProperties().
		return factory;
	}*/

	/*@Bean
	public BatchErrorHandler batchErrorHandler() {
		return new BatchLoggingErrorHandler();
	}*/


	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 1, (short) 3);
	}

	@Override
	@Transactional
	public void run(String... args) {
		IntStream.range(0, messagesCount).forEach(i -> {
			this.template.send(topicName, String.valueOf(i), new PracticalAdvice(String.valueOf(i), "A Practical Advice Number " + i, LocalDateTime.now()));
		});
		logger.info("All messages sent");
	}

	@PostConstruct
	public void pc() {
		// should not be in transaction
		if (!mongoTemplate.collectionExists(PracticalAdvice.class)) {
			mongoTemplate.createCollection(PracticalAdvice.class);
		}
		if (!mongoTemplate.collectionExists(DocumentDlq.class)) {
			mongoTemplate.createCollection(DocumentDlq.class);
		}

	}
}
