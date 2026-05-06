package cn.edu.tjrac.dao;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * 把字符串json 传化成object对象
 * @Classname SparkJsonDao
 * @Description TODO
 * @Date 2019/5/5 10:05
 * @Created by zgz
 */
public class SparkJsonDao implements Function<ConsumerRecord<String, String>, LogEntry> , Serializable {
    @Override
    public LogEntry call(ConsumerRecord<String, String> stringStringConsumerRecord) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(stringStringConsumerRecord.value(), LogEntry.class);
    }
}
