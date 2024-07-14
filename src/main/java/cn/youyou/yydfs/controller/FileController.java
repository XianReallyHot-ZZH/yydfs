package cn.youyou.yydfs.controller;

import cn.youyou.yydfs.syncer.FileSyncer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.UUID;

@Slf4j
@RestController
public class FileController {

    @Value("${yydfs.path}")
    private String uploadPath;

    @Value("${yydfs.isSync}")
    private boolean isSync;

    @Value("${yydfs.backupUrl:null}")
    private String backupUrl;

    @Autowired
    FileSyncer fileSyncer;

    /**
     * 文件上传
     * @param file
     * @param request
     * @return
     */
    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = request.getHeader("X-Filename");
        boolean sync = false;
        if (fileName == null || fileName.isEmpty()) {
            fileName = UUID.randomUUID().toString() + ext;
            sync = true;
        }
        String dir = fileName.substring(0, 2);
        log.info(">>> [FileController] originalFilename:{}, fileName:{}, file size = {}", originalFilename, fileName, file.getSize());
        File dest = new File(uploadPath + "/" + dir + "/" + fileName);
        log.info(">>> [FileController] upload file dir:{}", dest.getPath());
        file.transferTo(dest);
        if (sync) {
            fileSyncer.sync(dest, backupUrl, isSync);
        }
        return fileName;
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            String dir = name.substring(0, 2);
            File file = new File(uploadPath + "/" + dir + "/" + name);
            log.info(">>> [FileController] download file dir:{}", file.getPath());
            String fileName = file.getName();

            // 将文件写入输入流
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[16 * 1024];

            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");
            BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());

            while (fis.read(buffer) > 0) {
                outputStream.write(buffer);
            }
            fis.close();
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
