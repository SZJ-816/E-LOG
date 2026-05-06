package cn.edu.tjrac.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FlinkConfig {

    @Value("${bigdata.flink.rest-address:192.168.146.128}")
    private String flinkRestAddress;

    @Value("${bigdata.flink.rest-port:8081}")
    private int flinkRestPort;

    @Bean
    @ConditionalOnProperty(name = "bigdata.flink.enabled", havingValue = "true", matchIfMissing = false)
    public StreamExecutionEnvironment getEnv() {
        log.info("Creating Flink RemoteStreamEnvironment: {}:{}", flinkRestAddress, flinkRestPort);
        return new org.apache.flink.streaming.api.environment.RemoteStreamEnvironment(flinkRestAddress, flinkRestPort);
    }
}
