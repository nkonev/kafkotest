package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.*;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

// https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
@SpringBootApplication
@EnableBinding({Sink.class, Source.class})
public class KafkotestApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(KafkotestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KafkotestApplication.class, args);
	}

//	@Value("${tpd.topic-name}")
//	private String topicName;

//	@Autowired
//	private KafkaTemplate<String, Object> template;

	@Bean // https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.2.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html#_consuming_batches
	public PlatformTransactionManager transactionManager(BinderFactory binders) {
		ProducerFactory<byte[], byte[]> pf = ((KafkaMessageChannelBinder) binders.getBinder(null,
				MessageChannel.class)).getTransactionalProducerFactory();
		return new KafkaTransactionManager<>(pf);
	}

	private final int messagesCount = 1_000_000;

	@Autowired
	private Source source;

	@Transactional
	@StreamListener(Sink.INPUT)
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
			//@Payload PracticalAdvice payload
	) {
		logger.info("received: batch size: {}", payloads.size());
		for (PracticalAdvice payload: payloads) {
			if (payload.getIdentifier() % 10000 == 0 || payload.getIdentifier() == messagesCount - 1) {
				logger.info("received:  Payload: {}", payload);
			}
		}
	}
//
//	@Bean
//	public NewTopic adviceTopic() {
//		return new NewTopic(topicName, 1, (short) 3);
//	}

	@Override
	@Transactional
	public void run(String... args) {
		logger.info("Start sending messages");
		IntStream.range(0, messagesCount).forEach(i -> {
			this.source.output().send(MessageBuilder.withPayload(new PracticalAdvice("A Practical Advice Number " + i, i, LocalDateTime.now())).build());
			//logger.info("Sent {} msg", i);
		});
		logger.info("All messages sent");
	}
}
