package cn.youyou.yydfs.syncer;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件同步实现
 */
@Component
public class FileSyncerImpl implements FileSyncer {

    HttpSyncer httpSyncer = new HttpSyncer();

    @Override
    public boolean sync(File file, String backupUrl, boolean sync) {
        if (backupUrl == null || "null".equals(backupUrl)) {
            return false;
        }

        if (sync) {
            httpSyncer.sync(file, backupUrl, sync);
        }

        return true;
    }
}
