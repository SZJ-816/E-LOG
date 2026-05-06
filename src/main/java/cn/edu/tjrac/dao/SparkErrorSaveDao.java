package cn.edu.tjrac.dao;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Time;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SparkErrorSaveDao implements VoidFunction2<JavaRDD<String>, Time>, Serializable {

    // 回调接口：把错误数据传给 Driver（和UV格式完全一致）
    public interface ErrorCallback extends Serializable {
        void onResult(String path, String content);
    }

    private final ErrorCallback callback;

    // 构造方法传入回调
    public SparkErrorSaveDao(ErrorCallback callback) {
        this.callback = callback;
    }

    @Override
    public void call(JavaRDD<String> stringJavaRDD, Time time) {
        if (stringJavaRDD.isEmpty()) return;

        // 【仅做数据处理，无任何HDFS操作】
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String day = sdf.format(new Date());
        String content = String.join("\n", stringJavaRDD.collect()) + "\n";
        String path = "/spark/error/error-" + day + ".log";

        // 把数据传给 Driver 端执行写入
        callback.onResult(path, content);
    }
}