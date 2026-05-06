package cn.edu.tjrac.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class HDFSConfig {

    @Value("${bigdata.hdfs.uri:hdfs://172.20.0.6:8020}")
    private String hdfsUri;

    @Value("${bigdata.hdfs.user:root}")
    private String hdfsUser;

    @Bean(destroyMethod = "close")
    public FileSystem getHDFSClient() throws Exception {
        try {
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", hdfsUri);
            conf.set("dfs.permissions.enabled", "false");
            conf.set("dfs.client.use.datanode.hostname", "true");
            conf.set("dfs.datanode.use.datanode.hostname", "true");
            conf.set("dfs.namenode.rpc-bind-host", "0.0.0.0");
            conf.set("dfs.namenode.http-bind-host", "0.0.0.0");
            conf.set("dfs.client.failover.proxy.provider.default", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            conf.set("hadoop.security.authentication", "simple");
            
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
            UserGroupInformation.setLoginUser(ugi);
            
            FileSystem fs = FileSystem.get(new URI(hdfsUri), conf);
            log.info("HDFS client initialized successfully: {}", hdfsUri);
            return fs;
        } catch (Exception e) {
            log.error("Failed to initialize HDFS client: {}", e.getMessage(), e);
            throw e;
        }
    }
}
