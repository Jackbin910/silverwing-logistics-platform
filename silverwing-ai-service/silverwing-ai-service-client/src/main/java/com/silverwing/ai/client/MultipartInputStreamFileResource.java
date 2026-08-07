package com.silverwing.ai.client;

import org.springframework.core.io.AbstractResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 包装 MultipartFile 为 Spring 资源，重写 getFilename 以携带原文件名
 * <p>FastAPI 依赖 filename 识别内容，默认 InputStreamResource 无文件名会导致字段解析异常。</p>
 *
 * @author silverwing
 */
public class MultipartInputStreamFileResource extends AbstractResource {

    private final MultipartFile file;

    public MultipartInputStreamFileResource(MultipartFile file) {
        this.file = file;
    }

    @Override
    public String getDescription() {
        return "MultipartFile resource [filename=" + file.getOriginalFilename() + "]";
    }

    @Override
    public String getFilename() {
        return file.getOriginalFilename();
    }

    @Override
    public long contentLength() {
        return file.getSize();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return file.getInputStream();
    }
}
