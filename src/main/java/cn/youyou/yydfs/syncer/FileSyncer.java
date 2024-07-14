package cn.youyou.yydfs.syncer;

import java.io.File;

public interface FileSyncer {

    boolean sync(File file, String backupUrl, boolean sync);

}
