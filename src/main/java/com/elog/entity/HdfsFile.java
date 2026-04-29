package com.elog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * HDFS文件实体类
 */
@Data
@TableName("t_hdfs_file")
public class HdfsFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String path;
    
    private String fileName;
    
    private Long fileSize;
    
    private Short isDirectory;
    
    private LocalDateTime modificationTime;
    
    private LocalDateTime createTime;
}