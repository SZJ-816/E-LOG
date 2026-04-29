package com.elog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elog.entity.SyncTask;
import java.util.List;

/**
 * 同步任务服务接口
 */
public interface SyncTaskService extends IService<SyncTask> {
    
    /**
     * 创建同步任务
     */
    boolean createTask(SyncTask task);
    
    /**
     * 切换任务状态
     */
    boolean toggleStatus(Long id);
    
    /**
     * 手动执行一次任务
     */
    String executeTask(Long id) throws Exception;
    
    /**
     * 获取用户的所有任务
     */
    List<SyncTask> getUserTasks(Long userId);
}