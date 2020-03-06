package com.example.kafkotest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    @Autowired
    private KafkaTransactionManager kafkaTransactionManager;

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    @Override
    public TransactionManager annotationDrivenTransactionManager() {
        return new ChainedKafkaTransactionManager<>(kafkaTransactionManager, dataSourceTransactionManager);
    }

}
