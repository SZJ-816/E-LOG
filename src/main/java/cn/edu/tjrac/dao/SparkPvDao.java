package cn.edu.tjrac.dao;

import cn.edu.tjrac.entity.LogEntry;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Time;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PV 统计（纯计算，无HDFS写入，杜绝权限问题）
 */
public class SparkPvDao implements VoidFunction2<JavaRDD<LogEntry>, Time>, Serializable {

    // 回调接口：把计算结果传给 Driver 端写入HDFS
    public interface PVResultCallback extends Serializable {
        void onResult(long pv, String logLine, String day);
    }

    private final PVResultCallback callback;

    // 构造函数传入回调
    public SparkPvDao(PVResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void call(JavaRDD<LogEntry> rdd, Time time) {
        if (rdd.isEmpty()) return;

        // 1. Executor 只做计算（最快，最安全）
        long pv = rdd.count();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String day = sdf.format(new Date());
        String line = String.format("[%s] PV = %d%n", new Date(), pv);

        System.out.println("实时PV：" + pv);

        // 2. 把结果传给 Driver 端（Driver端写HDFS，权限100%生效）
        callback.onResult(pv, line, day);
    }
}