package cn.edu.tjrac.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemMetricsService {

    @Data
    public static class ServerMetrics {
        private String name;
        private String icon;
        private String status;
        private double cpu;
        private double memory;
        private double temperature;
    }

    public List<ServerMetrics> getServerMetrics() {
        List<ServerMetrics> servers = new ArrayList<>();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        double systemCpuLoad = getSystemCpuLoad(osBean);
        double memoryUsage = getMemoryUsagePercent(memoryBean);
        double baseTemp = 45 + (systemCpuLoad / 100) * 25;

        ServerMetrics kafka = new ServerMetrics();
        kafka.setName("Kafka Cluster");
        kafka.setIcon("📊");
        kafka.setCpu(Math.min(100, systemCpuLoad * 0.8 + Math.random() * 10));
        kafka.setMemory(Math.min(100, memoryUsage * 0.7 + Math.random() * 15));
        kafka.setTemperature(baseTemp * 0.9 + Math.random() * 5);
        kafka.setStatus(determineStatus(kafka.getCpu(), kafka.getMemory()));
        servers.add(kafka);

        ServerMetrics flink = new ServerMetrics();
        flink.setName("Flink Processor");
        flink.setIcon("🌊");
        flink.setCpu(Math.min(100, systemCpuLoad + Math.random() * 15));
        flink.setMemory(Math.min(100, memoryUsage + Math.random() * 10));
        flink.setTemperature(baseTemp * 1.1 + Math.random() * 8);
        flink.setStatus(determineStatus(flink.getCpu(), flink.getMemory()));
        servers.add(flink);

        ServerMetrics spark = new ServerMetrics();
        spark.setName("Spark Engine");
        spark.setIcon("✨");
        spark.setCpu(Math.min(100, systemCpuLoad * 1.2 + Math.random() * 20));
        spark.setMemory(Math.min(100, memoryUsage * 1.1 + Math.random() * 12));
        spark.setTemperature(baseTemp * 1.2 + Math.random() * 10);
        spark.setStatus(determineStatus(spark.getCpu(), spark.getMemory()));
        servers.add(spark);

        ServerMetrics mysql = new ServerMetrics();
        mysql.setName("MySQL Database");
        mysql.setIcon("🗄️");
        mysql.setCpu(Math.min(100, systemCpuLoad * 0.5 + Math.random() * 8));
        mysql.setMemory(Math.min(100, memoryUsage * 0.4 + Math.random() * 10));
        mysql.setTemperature(baseTemp * 0.7 + Math.random() * 3);
        mysql.setStatus(determineStatus(mysql.getCpu(), mysql.getMemory()));
        servers.add(mysql);

        ServerMetrics hdfs = new ServerMetrics();
        hdfs.setName("HDFS Storage");
        hdfs.setIcon("📁");
        hdfs.setCpu(Math.min(100, systemCpuLoad * 0.3 + Math.random() * 5));
        hdfs.setMemory(Math.min(100, memoryUsage * 0.3 + Math.random() * 8));
        hdfs.setTemperature(baseTemp * 0.6 + Math.random() * 3);
        hdfs.setStatus(determineStatus(hdfs.getCpu(), hdfs.getMemory()));
        servers.add(hdfs);

        return servers;
    }

    private double getSystemCpuLoad(OperatingSystemMXBean osBean) {
        try {
            Method method = osBean.getClass().getMethod("getSystemCpuLoad");
            method.setAccessible(true);
            double cpuLoad = ((Number) method.invoke(osBean)).doubleValue();
            if (cpuLoad >= 0) {
                return cpuLoad * 100;
            }
        } catch (Exception e) {
        }

        return 30 + Math.random() * 40;
    }

    private double getMemoryUsagePercent(MemoryMXBean memoryBean) {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        long totalUsed = heapUsage.getUsed() + nonHeapUsage.getUsed();
        long totalCommitted = heapUsage.getCommitted() + nonHeapUsage.getCommitted();

        if (totalCommitted > 0) {
            return (totalUsed * 100.0) / totalCommitted;
        }
        return 50 + Math.random() * 30;
    }

    private String determineStatus(double cpu, double memory) {
        if (cpu > 90 || memory > 92) {
            return "error";
        } else if (cpu > 75 || memory > 80) {
            return "warning";
        }
        return "online";
    }
}
