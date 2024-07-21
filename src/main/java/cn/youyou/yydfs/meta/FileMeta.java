package cn.youyou.yydfs.meta;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件元数据
 */
@Data
public class FileMeta {

    private String name;

    private String originalFileName;

    private long size;

    private String downloadUrl;

    private Map<String, String> tags = new HashMap<>();

    public FileMeta(String name, String originalFileName, long size, String downloadUrl) {
        this.name = name;
        this.originalFileName = originalFileName;
        this.size = size;
        this.downloadUrl = downloadUrl;
    }

}
