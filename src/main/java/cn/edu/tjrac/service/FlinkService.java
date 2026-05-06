package cn.edu.tjrac.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class FlinkService {

    @Autowired(required = false)
    private org.apache.flink.streaming.api.datastream.DataStream<String> kafkaStream;

    @Autowired(required = false)
    private org.apache.flink.streaming.api.environment.StreamExecutionEnvironment env;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @PostConstruct
    public void start() {
        if (kafkaStream == null || env == null) {
            log.info("Flink environment not available, FlinkService will not start");
            return;
        }
        executor.submit(() -> {
            try {
                setData();
            } catch (Exception e) {
                log.error("FlinkService error: {}", e.getMessage());
            }
        });
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

    public void setData() throws Exception {
        if (env == null || kafkaStream == null) {
            log.warn("Flink environment not available");
            return;
        }

        cn.edu.tjrac.dao.flink.FlinkLogParseMapDao parseMap = new cn.edu.tjrac.dao.flink.FlinkLogParseMapDao();
        cn.edu.tjrac.dao.flink.FlinkLogValidFilterFunctionDao validFilter = new cn.edu.tjrac.dao.flink.FlinkLogValidFilterFunctionDao();
        cn.edu.tjrac.dao.flink.FlinkResponseTimeMapDao responseTimeMap = new cn.edu.tjrac.dao.flink.FlinkResponseTimeMapDao();
        cn.edu.tjrac.dao.flink.FlinkEndpointMapDao endpointMap = new cn.edu.tjrac.dao.flink.FlinkEndpointMapDao();
        cn.edu.tjrac.dao.flink.FlinkTopNAllWindowDao topNWindow = new cn.edu.tjrac.dao.flink.FlinkTopNAllWindowDao();

        org.apache.flink.streaming.api.datastream.DataStream<cn.edu.tjrac.entity.LogEntry> logStream = kafkaStream
                .map(parseMap)
                .filter(validFilter);

        org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator<org.apache.flink.api.java.tuple.Tuple2<String, Integer>> rtStream = logStream.map(responseTimeMap);
        org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator<String> rtResult = rtStream
                .keyBy(t -> t.f0)
                .window(org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)))
                .sum(1)
                .map(t -> "响应时间分布: " + t.f0 + " = " + t.f1);
        rtResult.addSink(new cn.edu.tjrac.common.HdfsSinkFunction("/flink/rt")).setParallelism(1);

        org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator<org.apache.flink.api.java.tuple.Tuple2<String, Integer>> endpointStream = logStream.map(endpointMap);
        org.apache.flink.streaming.api.datastream.AllWindowedStream<org.apache.flink.api.java.tuple.Tuple2<String, Integer>, org.apache.flink.streaming.api.windowing.windows.TimeWindow> windowedStream = endpointStream
                .windowAll(org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)));

        org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator<java.util.List<org.apache.flink.api.java.tuple.Tuple2<String, Integer>>> topListStream = windowedStream.apply(topNWindow);
        org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator<String> topResult = topListStream.map(list -> {
            StringBuilder sb = new StringBuilder("热门接口Top3:\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(i + 1).append(". ").append(list.get(i).f0).append(" = ").append(list.get(i).f1).append("\n");
            }
            return sb.toString();
        });
        topResult.addSink(new cn.edu.tjrac.common.HdfsSinkFunction("/flink/top")).setParallelism(1);

        env.execute("LogProcessorFlink");
    }
}
