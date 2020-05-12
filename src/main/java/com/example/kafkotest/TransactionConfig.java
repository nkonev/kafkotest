package com.example.kafkotest;

import org.apache.ignite.Ignite;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    @Bean
    public SpringTransactionManager transactionManager(Ignite ignite, ApplicationContext applicationContext) {
        SpringTransactionManager transactionManager = new SpringTransactionManager();
        transactionManager.setApplicationContext(applicationContext);
        //transactionManager.setConfiguration(configuration);
        transactionManager.setIgniteInstanceName(IgniteCustomConfig.IN);
        transactionManager.setIgniteInstanceName(); // What ?
        return transactionManager;
    }

    @Autowired
    private KafkaTransactionManager kafkaTransactionManager;

    @Autowired
    private SpringTransactionManager igniteTransactionManager;

    @Override
    public TransactionManager annotationDrivenTransactionManager() {
        return new ChainedKafkaTransactionManager<>(kafkaTransactionManager, igniteTransactionManager);
    }

}
