package com.example.kafkotest;

import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    @Bean // https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.2.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html#_consuming_batches
    public KafkaTransactionManager transactionManager(BinderFactory binders) {
        ProducerFactory<byte[], byte[]> pf = ((KafkaMessageChannelBinder) binders.getBinder(null,
            MessageChannel.class)).getTransactionalProducerFactory();
        return new KafkaTransactionManager<>(pf);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
        MongoTransactionManager mongoTransactionManager = new MongoTransactionManager(dbFactory);
        mongoTransactionManager.setOptions(TransactionOptions.builder().writeConcern(WriteConcern.JOURNALED).build());
        return mongoTransactionManager;
    }

    @Autowired
    private KafkaTransactionManager kafkaTransactionManager;

    @Autowired
    private MongoTransactionManager mongoTransactionManager;

    @Override
    public TransactionManager annotationDrivenTransactionManager() {
        return new ChainedKafkaTransactionManager<>(kafkaTransactionManager, mongoTransactionManager);
    }

}
