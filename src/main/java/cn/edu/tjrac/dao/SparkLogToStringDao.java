package cn.edu.tjrac.dao;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.spark.api.java.function.Function;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

public class SparkLogToStringDao implements Function<LogEntry, String> , Serializable {
    @Override
    public String call(LogEntry logEntry) throws Exception {
        return logEntry.toString();
    }
}
