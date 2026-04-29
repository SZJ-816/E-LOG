package com.elog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elog.entity.SyncTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 同步任务Mapper
 */
@Mapper
public interface SyncTaskMapper extends BaseMapper<SyncTask> {
}