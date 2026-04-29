package com.elog.controller;

import com.elog.entity.SyncTask;
import com.elog.service.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步任务控制器
 */
@RestController
@RequestMapping("/api/task")
public class SyncTaskController {

    @Autowired
    private SyncTaskService syncTaskService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> result = new HashMap<>();
        List<SyncTask> list = syncTaskService.list();
        result.put("success", true);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody SyncTask task) {
        Map<String, Object> result = new HashMap<>();
        boolean success = syncTaskService.createTask(task);
        result.put("success", success);
        result.put("message", success ? "创建成功" : "创建失败");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = syncTaskService.toggleStatus(id);
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> execute(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            String message = syncTaskService.executeTask(id);
            result.put("success", true);
            result.put("message", message);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = syncTaskService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return ResponseEntity.ok(result);
    }
}