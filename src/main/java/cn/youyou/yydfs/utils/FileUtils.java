package cn.youyou.yydfs.utils;

import cn.youyou.yydfs.meta.FileMeta;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Slf4j
public class FileUtils {

    static String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * 项目启动时候初始化文件系统的存储目录结构
     *
     * @param uploadPath
     */
    public static void initDfsDirectory(String uploadPath) {
        log.info("init yydfs dirs...");
        File path = new File(uploadPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        // 输出256个文件夹，名称为十六进制
        for (int i = 0; i < 256; i++) {
            String dir = String.format("%02x", i);
            File dirPath = new File(uploadPath, dir);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
        }
    }

    /**
     * 根据文件名获取对应的文件类型
     *
     * @param fileName
     * @return
     */
    public static String getMimeType(String fileName) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(fileName);
        return contentType == null ? DEFAULT_MIME_TYPE : contentType;
    }

    /**
     * 获取文件的扩展名
     *
     * @param originalFilename
     * @return
     */
    public static String getExt(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    /**
     * 文件系统中统一命名后的文件名为UUID，UUID的前两位对应存储时的子文件夹名称
     *
     * @param fileName
     * @return
     */
    public static String getSubDir(String fileName) {
        return fileName.substring(0, 2);
    }

    /**
     * 对传入的文件名称进行统一的命名处理，返回新的文件名称
     *
     * @param fileName
     * @return
     */
    public static String getUUIDFile(String fileName) {
        return UUID.randomUUID().toString() + getExt(fileName);
    }

    /**
     * 将输入流写入输出流
     *
     * @param inputStream
     * @param outputStream
     */
    @SneakyThrows
    public static void output(InputStream inputStream, OutputStream outputStream) {
        BufferedInputStream fis = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[16 * 1024];

        // 读取文件
        while (fis.read(buffer) != -1) {
            outputStream.write(buffer);
        }
        outputStream.flush();
        outputStream.close();
        fis.close();
    }

    /**
     * 将文件写入输出流
     *
     * @param file
     * @param outputStream
     */
    @SneakyThrows
    public static void output(File file, OutputStream outputStream) {
        output(new FileInputStream(file), outputStream);
    }

    /**
     * 从指定的下载地址下载指定的文件
     *
     * @param downloadUrl
     * @param file
     */
    @SneakyThrows
    public static void download(String downloadUrl, File file) {
        log.info("download file from {}, file path: {}", downloadUrl, file.getAbsolutePath());
        // spring 提供的文件传输功能
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<Resource> exchange = restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, Resource.class);
        BufferedInputStream fis = new BufferedInputStream(exchange.getBody().getInputStream());
        OutputStream fos = new FileOutputStream(file);
        output(fis, fos);
    }

    /**
     * 将文件元数据写入指定的文件
     *
     * @param metaFile
     * @param meta
     */
    @SneakyThrows
    public static void writeMeta(File metaFile, FileMeta meta) {
        Files.writeString(Paths.get(metaFile.getAbsolutePath()),
                JSON.toJSONString(meta),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    /**
     * 将字符串写入指定的文件
     *
     * @param file
     * @param content
     */
    @SneakyThrows
    public static void writeString(File file, String content) {
        Files.writeString(Paths.get(file.getAbsolutePath()),
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    @SneakyThrows
    public static String readString(File file) {
        return FileCopyUtils.copyToString(new FileReader(file));
    }

    /**
     * 获取文件系统下统一命名的文件命对应的文件对象
     *
     * @param fileName
     * @return
     */
    public static File getFile(String uploadPath, String fileName) {
        return new File(uploadPath + "/" + getSubDir(fileName) + "/" + fileName);
    }

}
