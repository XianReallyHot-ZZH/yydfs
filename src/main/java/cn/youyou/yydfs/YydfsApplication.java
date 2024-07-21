package cn.youyou.yydfs;

import cn.youyou.yydfs.config.YYDfsConfigProperties;
import cn.youyou.yydfs.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(value = {YYDfsConfigProperties.class})
public class YydfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(YydfsApplication.class, args);
    }

    @Value("${yydfs.uploadPath}")
    private String uploadPath;

    @Bean
    ApplicationRunner runner() {
        return args -> {
            FileUtils.initDfsDirectory(uploadPath);
            log.info("init dfs directory success, YYDfs started");
        };
    }

}
