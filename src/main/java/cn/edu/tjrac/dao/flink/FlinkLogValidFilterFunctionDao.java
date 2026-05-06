package cn.edu.tjrac.dao.flink;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.flink.api.common.functions.FilterFunction;

import java.io.Serializable;

public class FlinkLogValidFilterFunctionDao implements FilterFunction<LogEntry>, Serializable {
    @Override
    public boolean filter(LogEntry logEntry) throws Exception {
        return logEntry != null && logEntry.getIp() != null;
    }
}
