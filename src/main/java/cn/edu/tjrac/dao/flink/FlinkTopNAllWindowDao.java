package cn.edu.tjrac.dao.flink;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlinkTopNAllWindowDao implements AllWindowFunction<Tuple2<String, Integer>, List<Tuple2<String, Integer>>, TimeWindow>, Serializable {
    @Override
    public void apply(TimeWindow timeWindow, Iterable<Tuple2<String, Integer>> input, Collector<List<Tuple2<String, Integer>>> collector) throws Exception {
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        input.forEach(list::add);
        list.sort((a, b) -> b.f1 - a.f1);
        int topN = Math.min(3, list.size());
        collector.collect(new ArrayList<>(list.subList(0, topN)));
    }
}
