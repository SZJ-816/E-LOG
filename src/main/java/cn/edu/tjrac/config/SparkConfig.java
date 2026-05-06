package cn.edu.tjrac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class SparkConfig {
    @Value("${bigdata.kafka.bootstrap-servers}")
    private String kafkaBootstrap;

    @Value("${bigdata.kafka.topic}")
    private String kafkaTopic;

    @Value("${bigdata.kafka.group-id}")
    private String kafkaGroupId;

    private JavaStreamingContext jssc = null;

    @Bean
    @Lazy
    public JavaStreamingContext getJavaStreamingContext() {
        try {
            SparkConf conf = new SparkConf()
                    .setAppName("LogProcessorSpark")
                    .setMaster("local[2]")
                    .set("spark.ui.enabled", "false")
                    .set("spark.driver.extraJavaOptions", "--add-opens java.base/sun.nio.ch=ALL-UNNAMED")
                    .set("spark.executor.extraJavaOptions", "--add-opens java.base/sun.nio.ch=ALL-UNNAMED");

            jssc = new JavaStreamingContext(conf, Durations.seconds(10));
            log.info("Spark JavaStreamingContext created successfully");
            return jssc;
        } catch (Exception e) {
            log.error("Failed to create Spark JavaStreamingContext: {}", e.getMessage());
            throw e;
        }
    }

    @Bean
    @Lazy
    public JavaInputDStream<ConsumerRecord<String, String>> getStream(JavaStreamingContext jssc){
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaBootstrap);
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("group.id", kafkaGroupId + "-spark");
        kafkaParams.put("auto.offset.reset", "latest");

        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(Collections.singletonList(kafkaTopic), kafkaParams));

        log.info("==============================================");
        log.info(stream.toString());
        return stream;
    }

    @PreDestroy
    public void shutdown() {
        if (jssc != null) {
            jssc.stop(true, true);
        }
    }
}
