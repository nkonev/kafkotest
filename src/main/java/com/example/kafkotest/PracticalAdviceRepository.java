package com.example.kafkotest;


import io.tpd.kafkaexample.PracticalAdvice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticalAdviceRepository extends CrudRepository<PracticalAdvice, String> {

}
