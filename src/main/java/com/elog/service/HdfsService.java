package com.elog.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * HDFS服务接口
 */
public interface HdfsService {
    
    /**
     * 获取目录文件列表
     */
    List<Map<String, Object>> listFiles(String path) throws Exception;
    
    /**
     * 上传文件到HDFS
     */
    boolean uploadFile(String path, MultipartFile file) throws Exception;
    
    /**
     * 下载HDFS文件
     */
    byte[] downloadFile(String path) throws Exception;
    
    /**
     * 删除文件或目录
     */
    boolean delete(String path) throws Exception;
    
    /**
     * 创建目录
     */
    boolean createDirectory(String path) throws Exception;
}