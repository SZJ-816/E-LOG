package com.elog.controller;

import com.elog.service.DataSourceService;
import com.elog.service.SyncTaskService;
import com.elog.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统/仪表盘控制器
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SyncTaskService syncTaskService;

    @GetMapping("/health")
    public ResponseEntity<Result> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(Result.success(info));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Result> dashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("datasourceCount", dataSourceService.count());
        stats.put("taskCount", syncTaskService.count());
        stats.put("hdfsConnected", true);
        stats.put("mysqlConnected", true);
        return ResponseEntity.ok(Result.success(stats));
    }
}