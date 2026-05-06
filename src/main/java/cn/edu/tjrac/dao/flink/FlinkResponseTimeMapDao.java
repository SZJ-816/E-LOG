package cn.edu.tjrac.dao.flink;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;

public class FlinkResponseTimeMapDao implements MapFunction<LogEntry, Tuple2<String, Integer>>, Serializable {
    @Override
    public Tuple2<String, Integer> map(LogEntry logEntry) throws Exception {
        int rt = logEntry.getResponseTime();
        String range = rt < 100 ? "<100ms" : (rt < 500 ? "100-500ms" : ">500ms");
        return Tuple2.of(range, 1);
    }
}
