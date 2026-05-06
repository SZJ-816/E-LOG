package cn.edu.tjrac.common;

import cn.edu.tjrac.config.HDFSConfig;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class SaveHDFSUtil {
    // 基础路径
    private final String hdfsBasePath = "";
    private final HDFSConfig hdfsConfig = new HDFSConfig();

    public void save(String hdfsPath, String content) throws Exception {
        Path fullPath = new Path(hdfsBasePath + hdfsPath);
        FileSystem fs = hdfsConfig.getHDFSClient();

        try {
            // 创建父目录
            Path parentPath = fullPath.getParent();
            if (!fs.exists(parentPath)) {
                fs.mkdirs(parentPath);
            }

            // 写入文件（自动创建/追加）
            try (FSDataOutputStream out = fs.exists(fullPath) ? fs.append(fullPath) : fs.create(fullPath)) {
                out.writeBytes(content);
                out.flush();
                System.out.println("HDFS写入成功：" + fullPath);
            }
        } finally {
            fs.close();
        }
    }
}