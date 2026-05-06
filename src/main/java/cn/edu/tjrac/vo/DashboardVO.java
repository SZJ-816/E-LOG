package cn.edu.tjrac.vo;

import lombok.Data;
import java.util.List;

@Data
public class DashboardVO {
    private Long totalPv;
    private Long totalUv;
    private Long todayErrors;
    private Double avgResponseTime;
    private Long onlineUsers;
    private List<HourlyStats> hourlyStats;
    private List<TopApiVO> topApis;
    private SystemHealthVO systemHealth;

    @Data
    public static class HourlyStats {
        private Integer hour;
        private Long pv;
        private Long uv;
    }

    @Data
    public static class TopApiVO {
        private String apiPath;
        private Long callCount;
        private Double avgTime;
        private Long errorCount;
    }

    @Data
    public static class SystemHealthVO {
        private String status;
        private Double cpuUsage;
        private Double memoryUsage;
        private Double diskUsage;
    }
}
