package com.elog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据源实体类
 */
@Data
@TableName("t_data_source")
public class DataSource {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    /** 数据源类型: MYSQL, POSTGRESQL, ORACLE, HDFS, API, FILE */
    private String type;
    
    /** 连接配置 JSON */
    private String config;
    
    /** 描述 */
    private String description;
    
    /** 创建用户ID */
    private Long createUserId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}