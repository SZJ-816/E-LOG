package com.elog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 同步任务实体类
 */
@Data
@TableName("t_sync_task")
public class SyncTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    /** 源数据源ID */
    private Long sourceId;
    
    /** 目标HDFS路径 */
    private String targetPath;
    
    /** Cron表达式，如: 0 0 * * * ? */
    private String cronExpr;
    
    /** 状态: 0-停用, 1-启用 */
    private Integer status;
    
    /** 上次执行时间 */
    private LocalDateTime lastRunTime;
    
    /** 上次执行状态: SUCCESS, FAILED */
    private String lastRunStatus;
    
    private Long createUserId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}