package cn.youyou.yydfs.controller;

import cn.youyou.yydfs.config.YYDfsConfigProperties;
import cn.youyou.yydfs.meta.FileMeta;
import cn.youyou.yydfs.syncer.HttpSyncer;
import cn.youyou.yydfs.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
@RestController
public class FileController {

    @Autowired
    private YYDfsConfigProperties configProperties;

    @Autowired
    private HttpSyncer httpSyncer;


    /**
     * 文件上传
     *
     * @param file
     * @param request
     * @return
     */
    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        // 处理文件
        boolean needSync = false;
        String fileName = request.getHeader(HttpSyncer.XFILENAME);
        String originalFileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {   // 请求头里没有文件名，说明是用户上传，并不是系统间的调用，需要进行统一命名，本地存储后，还要将文件同步到备份服务器
            needSync = true;
            fileName = FileUtils.getUUIDFile(originalFileName);
            log.info("receive upload file：{}", fileName);
        } else {    // 本次调用为系统间的调用，是主同步过来的请求
            String xor = request.getHeader(HttpSyncer.XORIGFILENAME);
            if (xor != null && !xor.isEmpty()) {
                originalFileName = xor;
            }
            log.info("receive sync file：{}", fileName);
        }

        // 本地存储
        File destFile = FileUtils.getFile(configProperties.getUploadPath(), fileName);
        file.transferTo(destFile);

        // 处理meta
        FileMeta fileMeta = new FileMeta(fileName, originalFileName, file.getSize(), configProperties.getDownloadUrl());
        if (configProperties.isAutoMd5()) {
            fileMeta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(destFile)));
        }
        FileUtils.writeMeta(new File(destFile.getAbsoluteFile() + ".meta"), fileMeta);

        // 同步到备份服务器
        if (needSync) {
            // 同步方式
            if (configProperties.isSyncBackup()) {
                try {
                    log.info("sync file to backup server...");
                    httpSyncer.sync(destFile, configProperties.getBackupUrl(), originalFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 同步失败转异步处理
                    log.error("sync file failed, try to async sync file to backup server...");

                }
            } else {    // 异步方式
                log.info("async sync file to backup server...");
            }
        }

        return fileName;
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @SneakyThrows
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        File file = FileUtils.getFile(configProperties.getUploadPath(), name);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(FileUtils.getMimeType(name));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        FileUtils.output(file, response.getOutputStream());
    }


    @SneakyThrows
    @RequestMapping("/meta")
    public String meta(String metaName) {
        return FileUtils.readString(FileUtils.getFile(configProperties.getUploadPath(), metaName));
    }


}
