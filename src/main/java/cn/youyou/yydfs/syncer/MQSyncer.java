package cn.youyou.yydfs.syncer;

import cn.youyou.yydfs.config.YYDfsConfigProperties;
import cn.youyou.yydfs.meta.FileMeta;
import cn.youyou.yydfs.utils.FileUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.awt.event.WindowFocusListener;
import java.io.File;

@Slf4j
@Component
public class MQSyncer {

    @Autowired
    private YYDfsConfigProperties configProperties;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    private String topic = "yydfs";

    /**
     * 将待同步的文件信息发送至mq
     *
     * @param meta
     */
    public void async(FileMeta meta) {
        Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(meta)).build();
        rocketMQTemplate.send(topic, message);
        log.info("send message:{} to mq", message);
    }


    /**
     * mq同步监听器,实现对文件的同步处理
     */
    @Component
    @RocketMQMessageListener(topic = "yydfs", consumerGroup = "${yydfs.group}")
    public class FileSyncerImpl implements RocketMQListener<MessageExt> {

        @Override
        public void onMessage(MessageExt message) {
            // 1. 从消息里拿到meta数据
            log.info(" ==> onMessage ID = {}", message.getMsgId());
            String json = new String(message.getBody());
            log.info(" ==> onMessage json = {}", json);
            FileMeta meta = JSON.parseObject(json, FileMeta.class);
            String downloadUrl = meta.getDownloadUrl();
            if(downloadUrl == null || downloadUrl.isEmpty()) {
                log.info(" ==> downloadUrl is empty.");
                return;
            }

            // 去重本机操作
            if(downloadUrl.equals(configProperties.getDownloadUrl())) {
                log.info(" ==> download url equals local url, ignore mq async task");
                return;
            }

            // 2. 写meta文件
            String dir = configProperties.getUploadPath() + "/" + FileUtils.getSubDir(meta.getName());
            File metaFile = new File(dir, meta.getName() + ".meta");
            if (metaFile.exists()) {
                log.info(" ==> meta file exists, ignore save: {}", metaFile.getAbsolutePath());
            } else {
                log.info(" ==> save meta file: {}", metaFile.getAbsolutePath());
                FileUtils.writeMeta(metaFile, meta);
            }

            // 3. 下载文件
            File file = new File(dir, meta.getName());
            if (file.exists() && file.length() == meta.getSize()) {
                log.info(" ==> file exists, ignore download: {}", file.getAbsolutePath());
                return;
            }

            String download = meta.getDownloadUrl() + "?name=" + meta.getName();
            FileUtils.download(download, file);
        }
    }



}
