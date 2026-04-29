package com.elog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elog.entity.DataSource;

/**
 * 数据源服务接口
 */
public interface DataSourceService extends IService<DataSource> {
    
    /**
     * 测试数据源连接
     */
    boolean testConnection(DataSource dataSource);
    
    /**
     * 获取用户的数据源列表
     */
}}