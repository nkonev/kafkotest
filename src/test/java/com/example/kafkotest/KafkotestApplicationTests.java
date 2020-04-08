package com.example.kafkotest;

import com.github.silaev.mongodb.replicaset.MongoDbReplicaSet;
import com.playtika.test.kafka.configuration.EmbeddedKafkaBootstrapConfiguration;
import com.playtika.test.kafka.properties.KafkaConfigurationProperties;
import io.tpd.kafkaexample.PracticalAdvice;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;

import java.time.LocalDateTime;

import static com.playtika.test.kafka.properties.KafkaConfigurationProperties.KAFKA_BEAN_NAME;
import static java.lang.String.format;

@SpringBootTest(classes = {KafkotestApplication.class, EmbeddedKafkaBootstrapConfiguration.class}//,
//		properties = {
//		"spring.kafka.bootstrap-servers=${embedded.kafka.brokerList}"
//}
)
@ContextConfiguration(initializers = {KafkotestApplicationTests.MongoInitializer.class
		, KafkotestApplicationTests.KafkaInitializer.class
})
public class KafkotestApplicationTests {

	/*public static class KafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

//		@Autowired
//		private KafkaConfigurationProperties kafkaProperties;

		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			GenericContainer kafka = (GenericContainer) configurableApplicationContext.getBean(KAFKA_BEAN_NAME);

			String host = kafka.getContainerIpAddress();
			String kafkaBrokerList = format("%s:%d", host, kafkaProperties.getBrokerPort());


			TestPropertyValues.of(
					"spring.kafka.bootstrap-servers: " + kafkaBrokerList
			).applyTo(configurableApplicationContext);
		}
	}*/

	static class MongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			if (MONGO_REPLICA_SET.isEnabled()) {
				TestPropertyValues.of(
						"spring.data.mongodb.uri: " + MONGO_REPLICA_SET.getReplicaSetUrl()
				).applyTo(configurableApplicationContext);
			}
		}
	}

	private static final MongoDbReplicaSet MONGO_REPLICA_SET = MongoDbReplicaSet.builder()
			//.replicaSetNumber(3)
			.mongoDockerImageName("mongo:4.2.3")
			//.addArbiter(true)
			//.addToxiproxy(true)
			.awaitNodeInitAttempts(30)
			.build();

	@BeforeAll
	public static void setUpAll() {
		MONGO_REPLICA_SET.start();
	}

	@AfterAll
	public static void tearDownAllAll() {
		MONGO_REPLICA_SET.stop();
	}

	@Autowired
	private KafkaTemplate<String, Object> template;

	@Value("${tpd.topic-name}")
	private String topicName;

	@Transactional
	@Test
	void contextLoads() {
		int i = 0;
		this.template.send(topicName, String.valueOf(i), new PracticalAdvice(String.valueOf(i), "A Practical Advice Number " + i, LocalDateTime.now()));
	}

}
