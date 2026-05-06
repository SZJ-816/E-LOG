package cn.edu.tjrac.dao;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Time;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SparkUVDao implements VoidFunction2<JavaRDD<String>, Time>, Serializable {

    // 回调接口：把计算结果传给 Driver
    public interface UVCallback extends Serializable {
        void onResult(String path, String content);
    }

    private final UVCallback callback;

    // 构造方法传入回调
    public SparkUVDao(UVCallback callback) {
        this.callback = callback;
    }

    @Override
    public void call(JavaRDD<String> stringJavaRDD, Time time) {
        if (stringJavaRDD.isEmpty()) return;

        // 【Executor 端：只做计算】
        long uv = stringJavaRDD.distinct().count();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String day = sdf.format(new Date());
        String line = String.format("[%s] UV = %d%n", new Date(), uv);
        String path = "/spark/uv/uv-" + day + ".log";

        System.out.println("实时UV：" + uv);

        // 【关键：把数据传给 Driver 端写入】
        callback.onResult(path, line);
    }
}