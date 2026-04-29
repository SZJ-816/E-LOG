package com.elog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elog.entity.DataSource;
import com.elog.mapper.DataSourceMapper;
import com.elog.service.DataSourceService;
import org.springframework.stereotype.Service;

/**
 * 数据源服务实现类
 */
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    @Override
    public boolean testConnection(DataSource dataSource) {
        // TODO: 根据不同数据源类型测试连接
        return true;
    }
}