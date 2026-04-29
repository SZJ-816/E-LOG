package com.elog.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elog.entity.DataSource;
import com.elog.mapper.DataSourceMapper;
import com.elog.service.DataSourceService;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据源服务实现类
 */
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    @Override
    public boolean testConnection(DataSource dataSource) {
        try {
            JSONObject config = JSON.parseObject(dataSource.getConfig());
            String type = dataSource.getType();
            
            switch (type) {
                case "MYSQL":
                    return testMysqlConnection(config);
                case "POSTGRESQL":
                    return testPostgresConnection(config);
                default:
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean testMysqlConnection(JSONObject config) {
        String host = config.getString("host");
        Integer port = config.getInteger("port");
        String database = config.getString("database");
        String username = config.getString("username");
        String password = config.getString("password");
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai",
                host, port != null ? port : 3306, database);
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testPostgresConnection(JSONObject config) {
        String host = config.getString("host");
        Integer port = config.getInteger("port");
        String database = config.getString("database");
        String username = config.getString("username");
        String password = config.getString("password");
        
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                host, port != null ? port : 5432, database);
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
}