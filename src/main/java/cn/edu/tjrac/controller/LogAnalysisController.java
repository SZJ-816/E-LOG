package cn.edu.tjrac.controller;

import cn.edu.tjrac.common.Result;
import cn.edu.tjrac.service.HdfsDataReader;
import cn.edu.tjrac.service.LogAnalysisService;
import cn.edu.tjrac.service.SystemMetricsService;
import cn.edu.tjrac.vo.DashboardVO;
import cn.edu.tjrac.vo.ErrorLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LogAnalysisController {

    @Autowired
    @Lazy
    private LogAnalysisService logAnalysisService;

    @Autowired
    @Lazy
    private SystemMetricsService systemMetricsService;

    @Autowired(required = false)
    @Lazy
    private HdfsDataReader hdfsDataReader;

    @GetMapping("/overview")
    public Result<DashboardVO> getOverview() {
        return Result.success(logAnalysisService.getOverview());
    }

    @GetMapping("/pvuv")
    public Result<List<DashboardVO.HourlyStats>> getPvUvStats() {
        return Result.success(logAnalysisService.getPvUvStats());
    }

    @GetMapping("/topN")
    public Result<List<DashboardVO.TopApiVO>> getTopApis(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return Result.success(logAnalysisService.getTopApis(limit));
    }

    @GetMapping("/errors")
    public Result<List<ErrorLogVO>> getRecentErrors(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return Result.success(logAnalysisService.getRecentErrors(limit));
    }

    @GetMapping("/health")
    public Result<DashboardVO.SystemHealthVO> getSystemHealth() {
        return Result.success(logAnalysisService.getSystemHealth());
    }

    @GetMapping("/servers")
    public Result<List<SystemMetricsService.ServerMetrics>> getServerMetrics() {
        return Result.success(systemMetricsService.getServerMetrics());
    }

    @GetMapping("/log-levels")
    public Result<List<Map<String, Object>>> getLogLevelStats() {
        return Result.success(logAnalysisService.getLogLevelStats());
    }

    @GetMapping("/hdfs/pv")
    public Result<String> getHdfsPv() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readTodayPv());
    }

    @GetMapping("/hdfs/uv")
    public Result<String> getHdfsUv() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readTodayUv());
    }

    @GetMapping("/hdfs/errors")
    public Result<String> getHdfsErrors() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readTodayErrors());
    }

    @GetMapping("/hdfs/rt")
    public Result<String> getHdfsRt() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readFlinkRt());
    }

    @GetMapping("/hdfs/top")
    public Result<String> getHdfsTop() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readFlinkTop());
    }

    @GetMapping("/hdfs/dashboard")
    public Result<HdfsDataReader.DashboardData> getHdfsDashboard() {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.readDashboardData());
    }

    @GetMapping("/hdfs/dirs")
    public Result<List<String>> listHdfsDirs(@RequestParam(defaultValue = "/spark") String path) {
        if (hdfsDataReader == null) {
            return Result.error("HDFS not available");
        }
        return Result.success(hdfsDataReader.listHdfsDirectories(path));
    }
}
