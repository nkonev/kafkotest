package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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

	private final int messagesCount = 1_000_000;

	private final static OkHttpClient client = new OkHttpClient.Builder()
			.readTimeout(60, TimeUnit.SECONDS)
			.connectTimeout(60 / 2, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.cache(null)
			.build();

	@Transactional
	@KafkaListener(topics = "${tpd.topic-name}", clientIdPrefix = "json")
	public void listenAsObject(
			@Payload List<PracticalAdvice> payloads
			//@Payload PracticalAdvice payload
	) {
		for (PracticalAdvice payload: payloads) {
			int integer = Integer.parseInt(payload.getIdentifier());
			if (integer%10000 == 0 || integer == messagesCount-1) {
				logger.info("received:  Payload: {}", payload);
			}
			//mongoTemplate.insert(payload);
		}
		mongoTemplate.insert(payloads, PracticalAdvice.class);
	}

	@Bean
	public NewTopic adviceTopic() {
		return new NewTopic(topicName, 1, (short) 3);
	}

	//@Transactional
	@GetMapping("/advice/{id}")
	public PracticalAdvice getFromDb(@PathVariable("id") String id) {
		PracticalAdvice byId = mongoTemplate.findById(id, PracticalAdvice.class);
		return byId;
	}

	@Value("${server.port}")
	private int port;

	@Value("${insert:true}")
	private boolean insert;

	@Override
	@Transactional
	public void run(String... args) {
		logger.info("Start sending messages");
		IntStream.range(0, messagesCount).forEach(i -> {
			if (insert) {
				this.template.send(topicName, String.valueOf(i), new PracticalAdvice(String.valueOf(i), "A Practical Advice Number " + i, LocalDateTime.now()));
			} else {
				if (i%10000 == 0 || i == messagesCount-1) {
					logger.info("Sending: {}", i);
				}
				final Request request = new Request.Builder()
						.url("http://localhost:" + port + "/advice/" + i)
						.build();
				logger.debug("Requesting " + i + request.toString());

				final Response response;
				try {
					response = client.newCall(request).execute();
					final String body = response.body().string();
					logger.debug("Successful get responce body: \n" + body);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		logger.info("All messages requested/sent");
	}

	@PostConstruct
	public void pc() {
		// should not be in transaction
		if (!mongoTemplate.collectionExists(PracticalAdvice.class)) {
			mongoTemplate.createCollection(PracticalAdvice.class);
		}
	}
}
