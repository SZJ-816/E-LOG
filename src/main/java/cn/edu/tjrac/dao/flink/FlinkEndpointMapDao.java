package cn.edu.tjrac.dao.flink;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;

public class FlinkEndpointMapDao implements MapFunction<LogEntry, Tuple2<String, Integer>>, Serializable {
    @Override
    public Tuple2<String, Integer> map(LogEntry logEntry) throws Exception {
        return Tuple2.of(logEntry.getEndpoint(), 1);
    }
}
