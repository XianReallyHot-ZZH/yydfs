package cn.youyou.yydfs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yydfs")
public class YYDfsConfigProperties {

    private String uploadPath;
    private String backupUrl;
    private String downloadUrl;
    private String group;
    private boolean autoMd5;
    private boolean syncBackup;

}
