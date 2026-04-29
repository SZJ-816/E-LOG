package com.elog.controller;

import com.elog.service.HdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HDFS文件控制器
 */
@RestController
@RequestMapping("/api/hdfs")
public class HdfsController {

    @Autowired
    private HdfsService hdfsService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listFiles(@RequestParam(required = false, defaultValue = "/") String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> files = hdfsService.listFiles(path);
            result.put("success", true);
            result.put("data", files);
            result.put("path", path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam String path, @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = hdfsService.uploadFile(path, file);
            result.put("success", success);
            result.put("message", success ? "上传成功" : "上传失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String path) {
        try {
            byte[] data = hdfsService.downloadFile(path);
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = hdfsService.delete(path);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/mkdir")
    public ResponseEntity<Map<String, Object>> createDirectory(@RequestParam String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = hdfsService.createDirectory(path);
            result.put("success", success);
            result.put("message", success ? "创建成功" : "创建失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}