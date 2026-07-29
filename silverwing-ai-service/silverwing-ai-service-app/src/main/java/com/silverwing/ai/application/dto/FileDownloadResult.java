package com.silverwing.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件下载结果
 * <p>携带原始文件字节与文件名，由触发层封装为 HTTP 响应流返回给前端下载。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件字节内容 */
    private byte[] content;

    /** 原始文件名（用于下载时的 Content-Disposition 文件名） */
    private String fileName;
}
