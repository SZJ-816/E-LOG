package com.elog.service.impl;

import com.elog.service.HdfsService;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HDFS服务实现类
 */
@Service
public class HdfsServiceImpl implements HdfsService {

    @Value("${hadoop.namenode:hdfs://192.168.146.128:9000}")
    private String namenode;

    @Override
    public List<Map<String, Object>> listFiles(String path) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        FileSystem fs = FileSystem.get(new java.net.URI(namenode), new org.apache.hadoop.conf.Configuration());
        Path hdfsPath = new Path(path.isEmpty() ? "/" : path);
        FileStatus[] statuses = fs.listStatus(hdfsPath);
        
        for (FileStatus status : statuses) {
            Map<String, Object> file = new HashMap<>();
            file.put("path", status.getPath().toString());
            file.put("fileName", status.getPath().getName());
            file.put("fileSize", status.getLen());
            file.put("isDirectory", status.isDirectory() ? 1 : 0);
            file.put("modificationTime", status.getModificationTime());
            result.add(file);
        }
        fs.close();
        return result;
    }

    @Override
    public boolean uploadFile(String path, MultipartFile file) throws Exception {
        FileSystem fs = FileSystem.get(new java.net.URI(namenode), new org.apache.hadoop.conf.Configuration());
        Path hdfsPath = new Path(path + "/" + file.getOriginalFilename());
        fs.create(hdfsPath);
        return true;
    }

    @Override
    public byte[] downloadFile(String path) throws Exception {
        FileSystem fs = FileSystem.get(new java.net.URI(namenode), new org.apache.hadoop.conf.Configuration());
        Path hdfsPath = new Path(path);
        InputStream in = fs.open(hdfsPath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        fs.close();
        return out.toByteArray();
    }

    @Override
    public boolean delete(String path) throws Exception {
        FileSystem fs = FileSystem.get(new java.net.URI(namenode), new org.apache.hadoop.conf.Configuration());
        boolean result = fs.delete(new Path(path), true);
        fs.close();
        return result;
    }

    @Override
    public boolean createDirectory(String path) throws Exception {
        FileSystem fs = FileSystem.get(new java.net.URI(namenode), new org.apache.hadoop.conf.Configuration());
        boolean result = fs.mkdirs(new Path(path));
        fs.close();
        return result;
    }
}