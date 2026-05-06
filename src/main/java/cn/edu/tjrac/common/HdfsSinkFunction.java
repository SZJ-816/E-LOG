package cn.edu.tjrac.common;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HdfsSinkFunction extends RichSinkFunction<String> {

    private final String hdfsUri;
    private final String subPath;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private transient FileSystem fs;
    private transient Path currentFilePath;
    private transient HdfsDataOutputStream outputStream;
    private transient String currentDate;

    public HdfsSinkFunction(String subPath) {
        this.hdfsUri = "hdfs://172.20.0.6:8020";
        this.subPath = subPath;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("fs.defaultFS", hdfsUri);
        conf.set("dfs.client.use.datanode.hostname", "false");
        conf.set("dfs.replication", "1");
        System.setProperty("HADOOP_USER_NAME", "root");
        fs = FileSystem.get(new URI(hdfsUri), conf);
        currentDate = LocalDate.now().format(DTF);
        currentFilePath = new Path("/log-analysis/" + subPath + "/log-" + currentDate + ".log");
        if (!fs.exists(currentFilePath.getParent())) {
            fs.mkdirs(currentFilePath.getParent());
        }
        if (fs.exists(currentFilePath)) {
            outputStream = (HdfsDataOutputStream) fs.append(currentFilePath);
        } else {
            outputStream = (HdfsDataOutputStream) fs.create(currentFilePath, true);
        }
        System.out.println("[HdfsSink] Opened - Path: " + currentFilePath);
    }

    private void checkAndRollFile() throws Exception {
        String newDate = LocalDate.now().format(DTF);
        if (!newDate.equals(currentDate)) {
            if (outputStream != null) {
                outputStream.hsync();
                outputStream.close();
            }
            currentDate = newDate;
            currentFilePath = new Path("/log-analysis/" + subPath + "/log-" + currentDate + ".log");
            if (!fs.exists(currentFilePath.getParent())) {
                fs.mkdirs(currentFilePath.getParent());
            }
            if (fs.exists(currentFilePath)) {
                outputStream = (HdfsDataOutputStream) fs.append(currentFilePath);
            } else {
                outputStream = (HdfsDataOutputStream) fs.create(currentFilePath, true);
            }
        }
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        checkAndRollFile();
        outputStream.writeBytes(value + "\n");
        outputStream.hsync();
    }

    @Override
    public void close() throws Exception {
        if (outputStream != null) {
            outputStream.hsync();
            outputStream.close();
        }
        if (fs != null) {
            fs.close();
        }
    }
}
