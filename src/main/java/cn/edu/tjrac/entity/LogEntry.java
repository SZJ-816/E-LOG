package cn.edu.tjrac.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class LogEntry implements Serializable {
    private long timestamp;
    private String ip;
    private String method;
    private String endpoint;
    private Integer statusCode;
    private Integer responseTime;
}
