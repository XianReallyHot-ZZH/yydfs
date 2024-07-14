package cn.youyou.yydfs.syncer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * 使用http完成文件同步
 */
@Slf4j
public class HttpSyncer implements FileSyncer{
    @Override
    public boolean sync(File file, String backupUrl, boolean sync) {
        // spring 提供的文件传输功能
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Filename", file.getName());
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(builder.build(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(backupUrl, httpEntity, String.class);
        log.info("sync result: {}", responseEntity.getBody());

        return true;
    }


    public static void main(String[] args) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor("a.png");
        System.out.println(contentType);
    }
}
