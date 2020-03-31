package com.example.kafkotest;

import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeEditor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

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
        return new ChainedKafkaTransactionManager<>(kafkaTransactionManager
                //, mongoTransactionManager
        );
    }

    /*@Bean(name = "transactionInterceptor")
    public TransactionInterceptor transactionInterceptor(PlatformTransactionManager platformTransactionManager) {
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        transactionInterceptor.setTransactionManager(platformTransactionManager);
//        Properties transactionAttributes = new Properties();
//        transactionAttributes.setProperty("*", "PROPAGATION_REQUIRED,-Throwable");
//        transactionAttributes.setProperty("tranNew*", "PROPAGATION_REQUIRES_NEW,-Throwable");
//        transactionInterceptor.setTransactionAttributes(transactionAttributes);
        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        atas.getTransactionAttribute()
//        TransactionAttributeEditor transactionAttributeEditor = new TransactionAttributeEditor();
//        transactionAttributeEditor.setAsText("");
//        NameMatchTransactionAttributeSource nameMatchTransactionAttributeSource = new NameMatchTransactionAttributeSource();
//        nameMatchTransactionAttributeSource.setNameMap();
//        transactionInterceptor.setTransactionAttributeSource(nameMatchTransactionAttributeSource);
        return transactionInterceptor;
    }*/
}
