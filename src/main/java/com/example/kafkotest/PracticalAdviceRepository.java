package com.example.kafkotest;

import io.tpd.kafkaexample.PracticalAdvice;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryConfig(cacheName = "practicalAdvice")
public interface PracticalAdviceRepository extends IgniteRepository <PracticalAdvice, String> {
}
