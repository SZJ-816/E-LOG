package cn.edu.tjrac.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HdfsDataReader {

    private static final String HDFS_BASE_PATH = "/log-analysis";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired(required = false)
    private FileSystem fs;

    public String readTodayPv() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return readFileContent(HDFS_BASE_PATH + "/spark/pv/pv-" + today + ".log");
    }

    public String readTodayUv() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return readFileContent(HDFS_BASE_PATH + "/spark/uv/uv-" + today + ".log");
    }

    public String readTodayErrors() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return readFileContent(HDFS_BASE_PATH + "/spark/error/error-" + today + ".log");
    }

    public String readFlinkRt() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return readFileContent(HDFS_BASE_PATH + "/flink/rt/log-" + today + ".log");
    }

    public String readFlinkTop() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return readFileContent(HDFS_BASE_PATH + "/flink/top/log-" + today + ".log");
    }

    public List<String> listHdfsDirectories(String path) {
        List<String> result = new ArrayList<>();
        if (fs == null) {
            log.warn("HDFS FileSystem is not available");
            return result;
        }
        try {
            Path dirPath = new Path(HDFS_BASE_PATH + path);
            if (fs.exists(dirPath)) {
                FileStatus[] statuses = fs.listStatus(dirPath);
                for (FileStatus status : statuses) {
                    result.add(status.getPath().getName() + (status.isDirectory() ? "/" : ""));
                }
            }
        } catch (Exception e) {
            log.error("Error listing HDFS directory {}: {}", path, e.getMessage());
        }
        return result;
    }

    public DashboardData readDashboardData() {
        DashboardData data = new DashboardData();
        data.setTimestamp(System.currentTimeMillis());

        String pvContent = readTodayPv();
        data.setPv(extractPvFromLog(pvContent));

        String uvContent = readTodayUv();
        data.setUv(extractUvFromLog(uvContent));

        String rtContent = readFlinkRt();
        data.setRtDistribution(parseRtDistribution(rtContent));

        String topContent = readFlinkTop();
        data.setTopApis(parseTopApis(topContent));

        return data;
    }

    private String readFileContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            if (fs == null) {
                log.warn("HDFS FileSystem is not initialized, skipping read for: {}", filePath);
                return "";
            }
            Path path = new Path(filePath);
            if (!fs.exists(path)) {
                log.debug("File not found: {}", filePath);
                return "";
            }

            try (FSDataInputStream fis = fs.open(path);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                log.info("Successfully read HDFS file: {}, content length: {}", filePath, content.length());
            }
        } catch (Exception e) {
            log.error("Error reading HDFS file {}: {}", filePath, e.getMessage(), e);
        }
        return content.toString();
    }

    private long extractPvFromLog(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        long totalPv = 0;
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("PV")) {
                try {
                    String numStr = line.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        totalPv += Long.parseLong(numStr);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return totalPv;
    }

    private long extractUvFromLog(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        long totalUv = 0;
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("UV")) {
                try {
                    String numStr = line.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        totalUv += Long.parseLong(numStr);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return totalUv;
    }

    private List<RtDistribution> parseRtDistribution(String content) {
        List<RtDistribution> result = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return result;
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("响应时间分布")) {
                try {
                    String[] parts = line.split("=");
                    if (parts.length >= 2) {
                        String range = parts[0].replace("响应时间分布:", "").trim();
                        int count = Integer.parseInt(parts[1].trim());
                        RtDistribution rt = new RtDistribution();
                        rt.setRange(range);
                        rt.setCount(count);
                        result.add(rt);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private List<TopApi> parseTopApis(String content) {
        List<TopApi> result = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return result;
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("热门接口Top3")) {
                continue;
            }
            if (line.matches("^\\d+\\..*")) {
                try {
                    String[] parts = line.substring(line.indexOf(".") + 1).split("=");
                    if (parts.length >= 2) {
                        TopApi api = new TopApi();
                        api.setEndpoint(parts[0].trim());
                        api.setCount(Integer.parseInt(parts[1].trim()));
                        result.add(api);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    @lombok.Data
    public static class DashboardData {
        private long timestamp;
        private long pv;
        private long uv;
        private List<RtDistribution> rtDistribution;
        private List<TopApi> topApis;
    }

    @lombok.Data
    public static class RtDistribution {
        private String range;
        private int count;
    }

    @lombok.Data
    public static class TopApi {
        private String endpoint;
        private int count;
    }
}
