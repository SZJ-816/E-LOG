package cn.edu.tjrac.dao.flink;

import cn.edu.tjrac.entity.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

import java.io.Serializable;

public class FlinkLogParseMapDao implements MapFunction<String, LogEntry>, Serializable {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Override
    public LogEntry map(String json) throws Exception {
        try {
            LogEntry log = MAPPER.readValue(json, LogEntry.class);
            if (log.getEndpoint() == null) log.setEndpoint("/default/api");
            if (log.getResponseTime() == null) log.setResponseTime(50);
            if (log.getStatusCode() == null) log.setStatusCode(200);
            return log;
        } catch (Exception e) {
            return new LogEntry();
        }
    }
}
