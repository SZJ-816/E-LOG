package cn.edu.tjrac.task;

import cn.edu.tjrac.entity.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SparkPVUVTask {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private FileSystem fs;

    @Value("${bigdata.kafka.bootstrap-servers:192.168.146.128:9092}")
    private String kafkaBootstrap;

    private final Map<String, Long> pvCountMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uvCountMap = new ConcurrentHashMap<>();
    private final Map<String, Long> errorCountMap = new ConcurrentHashMap<>();

    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = false;

    @Scheduled(fixedDelay = 1000)
    public void consumeKafka() {
        if (running) return;
        try {
            running = true;
            Properties props = new Properties();
            props.put("bootstrap.servers", kafkaBootstrap);
            props.put("group.id", "spark-pvuv-consumer");
            props.put("auto.offset.reset", "latest");
            props.put("enable.auto.commit", "true");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("log-topic"));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record.value());
                }
            }
        } catch (Exception e) {
            log.warn("Kafka consumer error: {}", e.getMessage());
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            running = false;
        }
    }

    private void processRecord(String value) {
        try {
            LogEntry logEntry = objectMapper.readValue(value, LogEntry.class);
            String minute = Instant.ofEpochMilli(logEntry.getTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            pvCountMap.merge(minute, 1L, Long::sum);

            uvCountMap.computeIfAbsent(minute, k -> ConcurrentHashMap.newKeySet())
                    .add(logEntry.getIp());

            if (logEntry.getStatusCode() >= 400) {
                errorCountMap.merge(minute, 1L, Long::sum);
            }
        } catch (Exception ignored) {
        }
    }

    @Scheduled(fixedRate = 60000)
    public void saveStatsToHDFS() {
        if (fs == null) {
            log.debug("HDFS FileSystem not available, skipping stats save");
            return;
        }
        try {
            String day = LocalDate.now().format(DATE_DTF);
            LocalDateTime now = LocalDateTime.now();
            String currentMinute = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            String pvPath = "/log-analysis/spark/pv/pv-" + day + ".log";
            Long pvCount = pvCountMap.getOrDefault(currentMinute, 0L);
            writeStatsToHDFS(pvPath, "time, pv\n", currentMinute, pvCount);

            String uvPath = "/log-analysis/spark/uv/uv-" + day + ".log";
            Long uvCount = uvCountMap.containsKey(currentMinute) ? (long) uvCountMap.get(currentMinute).size() : 0L;
            writeStatsToHDFS(uvPath, "time, uv\n", currentMinute, uvCount);

            String errorPath = "/log-analysis/spark/error/error-" + day + ".log";
            Long errorCount = errorCountMap.getOrDefault(currentMinute, 0L);
            writeStatsToHDFS(errorPath, "time, error_count\n", currentMinute, errorCount);

            log.info("[SparkPVUV] Saved stats for {} - PV: {}, UV: {}, Error: {}", currentMinute, pvCount, uvCount, errorCount);
        } catch (Exception e) {
            log.error("[SparkPVUV] Error saving stats: {}", e.getMessage());
        }
    }

    private void writeStatsToHDFS(String path, String header, String minute, Long count) throws Exception {
        Path hdfsPath = new Path(path);
        FSDataOutputStream outputStream;

        if (fs.exists(hdfsPath)) {
            outputStream = fs.append(hdfsPath);
        } else {
            Path parent = hdfsPath.getParent();
            if (!fs.exists(parent)) {
                fs.mkdirs(parent);
            }
            outputStream = fs.create(hdfsPath, true);
            outputStream.writeBytes(header);
        }

        outputStream.writeBytes(minute + ", " + count + "\n");
        outputStream.hsync();
        outputStream.close();
    }
}
