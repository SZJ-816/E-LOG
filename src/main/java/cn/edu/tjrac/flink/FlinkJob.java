package cn.edu.tjrac.flink;

import cn.edu.tjrac.common.HdfsSinkFunction;
import cn.edu.tjrac.dao.flink.*;
import cn.edu.tjrac.entity.LogEntry;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class FlinkJob {

    private static final String KAFKA_BOOTSTRAP = "192.168.146.128:9092";
    private static final String KAFKA_TOPIC = "log-topic";
    private static final String KAFKA_GROUP = "log-analysis-group-flink";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_GROUP);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                KAFKA_TOPIC,
                new StringDeserializerSchema(),
                props
        );
        kafkaConsumer.setStartFromLatest();

        DataStream<String> kafkaStream = env.addSource(kafkaConsumer);

        DataStream<LogEntry> logStream = kafkaStream
                .map(new FlinkLogParseMapDao())
                .filter(new FlinkLogValidFilterFunctionDao());

        SingleOutputStreamOperator<Tuple2<String, Integer>> rtStream = logStream.map(new FlinkResponseTimeMapDao());
        SingleOutputStreamOperator<String> rtResult = rtStream
                .keyBy(new ResponseTimeKeySelector())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .sum(1)
                .map(new ResponseTimeResultMapper());
        rtResult.addSink(new HdfsSinkFunction("/flink/rt"));

        SingleOutputStreamOperator<Tuple2<String, Integer>> endpointStream = logStream.map(new FlinkEndpointMapDao());
        AllWindowedStream<Tuple2<String, Integer>, TimeWindow> windowedStream = endpointStream
                .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10)));

        SingleOutputStreamOperator<List<Tuple2<String, Integer>>> topListStream = windowedStream.apply(new FlinkTopNAllWindowDao());
        SingleOutputStreamOperator<String> topResult = topListStream.map(new TopNResultMapper());
        topResult.addSink(new HdfsSinkFunction("/flink/top"));

        env.execute("LogProcessorFlink");
    }

    private static class StringDeserializerSchema implements KeyedDeserializationSchema<String> {
        @Override
        public String deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
            return new String(message);
        }

        @Override
        public boolean isEndOfStream(String nextElement) {
            return false;
        }

        @Override
        public TypeInformation<String> getProducedType() {
            return TypeExtractor.getForClass(String.class);
        }
    }

    private static class ResponseTimeKeySelector implements KeySelector<Tuple2<String, Integer>, String> {
        @Override
        public String getKey(Tuple2<String, Integer> t) throws Exception {
            return t.f0;
        }
    }

    private static class ResponseTimeResultMapper implements MapFunction<Tuple2<String, Integer>, String> {
        @Override
        public String map(Tuple2<String, Integer> t) throws Exception {
            return "响应时间分布: " + t.f0 + " = " + t.f1;
        }
    }

    private static class TopNResultMapper implements MapFunction<List<Tuple2<String, Integer>>, String> {
        @Override
        public String map(List<Tuple2<String, Integer>> list) throws Exception {
            StringBuilder sb = new StringBuilder("热门接口Top3:\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(i + 1).append(". ").append(list.get(i).f0).append(" = ").append(list.get(i).f1).append("\n");
            }
            return sb.toString();
        }
    }
}
