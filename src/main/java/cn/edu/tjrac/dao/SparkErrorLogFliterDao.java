package cn.edu.tjrac.dao;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.spark.api.java.function.Function;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

public class SparkErrorLogFliterDao implements Function<LogEntry, Boolean> , Serializable {
    @Override
    public Boolean call(LogEntry logEntry) throws Exception {
        return  logEntry.getStatusCode() >= 400;
    }
}
