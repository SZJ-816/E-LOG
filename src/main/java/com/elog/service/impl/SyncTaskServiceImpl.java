package com.elog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elog.entity.DataSource;
import com.elog.entity.SyncTask;
import com.elog.mapper.SyncTaskMapper;
import com.elog.service.DataSourceService;
import com.elog.service.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步任务服务实现类
 */
@Service
public class SyncTaskServiceImpl extends ServiceImpl<SyncTaskMapper, SyncTask> implements SyncTaskService {

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    public boolean createTask(SyncTask task) {
        task.setStatus(1);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return save(task);
    }

    @Override
    public boolean toggleStatus(Long id) {
        SyncTask task = getById(id);
        if (task != null) {
            task.setStatus(task.getStatus() == 1 ? 0 : 1);
            task.setUpdateTime(LocalDateTime.now());
            return updateById(task);
        }
        return false;
    }

    @Override
    public String executeTask(Long id) throws Exception {
        SyncTask task = getById(id);
        if (task == null) {
            return "任务不存在";
        }
        
        DataSource source = dataSourceService.getById(task.getSourceId());
        if (source == null) {
            return "数据源不存在";
        }
        
        // TODO: 根据数据源类型执行同步
        // 目前模拟执行
        task.setLastRunTime(LocalDateTime.now());
        task.setLastRunStatus("SUCCESS");
        task.setUpdateTime(LocalDateTime.now());
        updateById(task);
        
        return "执行成功";
    }

    @Override
    public List<SyncTask> getUserTasks(Long userId) {
        return list(null);
    }
}