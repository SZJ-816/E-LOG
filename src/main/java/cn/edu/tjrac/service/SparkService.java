package cn.edu.tjrac.service;

import cn.edu.tjrac.common.SaveHDFSUtil;
import cn.edu.tjrac.dao.*;
import cn.edu.tjrac.entity.LogEntry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Service
@Lazy
public class SparkService {
    private JavaInputDStream<ConsumerRecord<String, String>> stream;
    private JavaStreamingContext jssc;
    private final SaveHDFSUtil saveHDFSUtil = new SaveHDFSUtil();
    private boolean sparkAvailable = false;

    @Resource
    @Lazy
    public void setStream(JavaInputDStream<ConsumerRecord<String, String>> stream) {
        this.stream = stream;
    }

    @Resource
    @Lazy
    public void setJssc(JavaStreamingContext jssc) {
        this.jssc = jssc;
    }

    @PostConstruct
    public void init() {
        sparkAvailable = true;
        System.out.println("Spark service initialized successfully");
        new Thread(() -> {
            try {
                pv();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Spark-Thread").start();
    }

    @PreDestroy
    public void stop() {
        if (jssc != null) {
            jssc.stop(true, true);
        }
    }


    public void pv() throws InterruptedException {
        if (!sparkAvailable || stream == null || jssc == null) {
            return;
        }
        JavaDStream<LogEntry> logStream = stream
                .filter(new SparkEmptyFilyerDao())
                .map(new SparkJsonDao());

        logStream.foreachRDD(new SparkPvDao((pv, logLine, day) -> {
            try {
                String hdfsPath = "/spark/pv/pv-" + day + ".log";
                saveHDFSUtil.save(hdfsPath, logLine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        logStream.map(new SparkIPDao()).foreachRDD(new SparkUVDao((hdfsPath, content) -> {
            try {
                saveHDFSUtil.save(hdfsPath, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        logStream.filter(new SparkErrorLogFliterDao()).map(new SparkLogToStringDao()).foreachRDD(new SparkErrorSaveDao((hdfsPath, content) -> {
            try {
                saveHDFSUtil.save(hdfsPath, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        jssc.start();
        jssc.awaitTermination();
    }
}
