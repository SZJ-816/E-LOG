package cn.edu.tjrac.dao;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function;
import org.springframework.stereotype.Repository;

import java.io.Serializable;


public class SparkEmptyFilyerDao implements Function<ConsumerRecord<String, String>, Boolean>, Serializable {
    @Override
    public Boolean call(ConsumerRecord<String, String> stringStringConsumerRecord) throws Exception {
        String value = stringStringConsumerRecord.value();
        return value != null && !value.trim().isEmpty();
    }
}
