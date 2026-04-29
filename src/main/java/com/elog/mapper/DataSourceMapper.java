package com.elog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elog.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源Mapper
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {
}