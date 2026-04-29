package com.elog.controller;

import com.elog.entity.DataSource;
import com.elog.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> result = new HashMap<>();
        List<DataSource> list = dataSourceService.list();
        result.put("success", true);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody DataSource dataSource) {
        Map<String, Object> result = new HashMap<>();
        boolean success = dataSourceService.save(dataSource);
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = dataSourceService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody DataSource dataSource) {
        Map<String, Object> result = new HashMap<>();
        boolean success = dataSourceService.testConnection(dataSource);
        result.put("success", success);
        result.put("message", success ? "连接成功" : "连接失败");
        return ResponseEntity.ok(result);
    }
}