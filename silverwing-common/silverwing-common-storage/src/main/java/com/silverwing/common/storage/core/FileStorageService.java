package com.silverwing.common.storage.core;

import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.storage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务（基于 S3 协议，兼容 RustFS / MinIO）
 * <p>
 * 提供上传、下载、删除、存在性检查、文件大小查询等能力，并在启动时按需自动建桶。
 * 上传后的文件以 {@code prefix/yyyy/MM/dd/uuid_拼音文件名} 作为对象 Key 落盘，
 * 便于在对象存储控制台按日期与业务前缀检索。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
public class FileStorageService {

    private final S3Client s3Client;

    private final StorageProperties storageConfig;

    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 汉字转拼音格式：小写、无声调 */
    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = new HanyuPinyinOutputFormat();

    static {
        PINYIN_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        PINYIN_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public FileStorageService(S3Client s3Client, StorageProperties storageConfig) {
        this.s3Client = s3Client;
        this.storageConfig = storageConfig;
    }

    /**
     * 启动后检查并自动创建存储桶（配置关闭时跳过）。
     * 注意：建桶失败仅记录警告，不阻塞应用启动；真正上传时若存储仍不可达，
     * 会在请求处理中抛出友好的 BusinessException，便于本地开发时存储未就绪也能先起服务。
     */
    @PostConstruct
    public void init() {
        if (!storageConfig.isAutoCreateBucket()) {
            log.info("存储桶启动检查已关闭: bucket={}", storageConfig.getBucket());
            return;
        }
        try {
            ensureBucketExists();
        } catch (BusinessException e) {
            log.warn("启动检查存储桶失败，应用将继续启动（首次上传时若仍不可达将报错）: bucket={}, reason={}",
                    storageConfig.getBucket(), e.getMessage());
        }
    }

    /**
     * 上传知识库文档（RAG 场景使用 rag 前缀）
     *
     * @param file 上传的文件
     * @return 对象存储 Key（如 rag/2026/07/21/ab12cd34_文件名.pdf）
     */
    public String uploadKnowledgeBase(MultipartFile file) {
        return uploadFile(file, "rag");
    }

    /**
     * 通用文件上传
     *
     * @param file   上传的文件
     * @param prefix 业务前缀（如 rag / resumes）
     * @return 对象存储 Key
     */
    public String upload(MultipartFile file, String prefix) {
        return uploadFile(file, prefix);
    }

    /**
     * 下载文件字节
     *
     * @param fileKey 对象存储 Key
     * @return 文件字节数组
     */
    public byte[] downloadFile(String fileKey) {
        if (!fileExists(fileKey)) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.download.not.found", fileKey);
        }
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .key(fileKey)
                    .build();
            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (S3Exception e) {
            log.error("下载文件失败: {} - {}", fileKey, e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.download.failed", e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileKey 对象存储 Key
     * @return 存在返回 true
     */
    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .key(fileKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.warn("检查文件存在性失败: {} - {}", fileKey, e.getMessage());
            return false;
        }
    }

    /**
     * 获取文件大小（字节）
     *
     * @param fileKey 对象存储 Key
     * @return 文件大小
     */
    public long getFileSize(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .key(fileKey)
                    .build();
            return s3Client.headObject(request).contentLength();
        } catch (S3Exception e) {
            log.error("获取文件大小失败: {} - {}", fileKey, e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.info.failed", e.getMessage());
        }
    }

    /**
     * 删除文件（空 Key 或不存在时静默跳过）
     *
     * @param fileKey 对象存储 Key
     */
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            log.debug("文件键为空，跳过删除");
            return;
        }
        if (!fileExists(fileKey)) {
            log.warn("文件不存在，跳过删除: {}", fileKey);
            return;
        }
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("文件删除成功: {}", fileKey);
        } catch (S3Exception e) {
            log.error("删除文件失败: {} - {}", fileKey, e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.delete.failed", e.getMessage());
        }
    }

    /**
     * 拼接文件访问 URL（用于前端预览 / 下载跳转）
     *
     * @param fileKey 对象存储 Key
     * @return 文件访问 URL
     */
    public String getFileUrl(String fileKey) {
        return String.format("%s/%s/%s", storageConfig.getEndpoint(), storageConfig.getBucket(), fileKey);
    }

    /**
     * 通用文件上传实现
     */
    private String uploadFile(MultipartFile file, String prefix) {
        String originalFilename = file.getOriginalFilename();
        String fileKey = generateFileKey(originalFilename, prefix);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("文件上传成功: {} -> {}", originalFilename, fileKey);
            return fileKey;
        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.read.failed");
        } catch (S3Exception e) {
            log.error("上传文件到对象存储失败: {}", e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.upload.failed", e.getMessage());
        }
    }

    /**
     * 确保存储桶存在（不存在则创建）
     */
    public void ensureBucketExists() {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .build();
            s3Client.headBucket(request);
            log.info("存储桶已存在: {}", storageConfig.getBucket());
        } catch (NoSuchBucketException e) {
            createBucket();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                createBucket();
                return;
            }
            log.error("检查存储桶失败: bucket={}, error={}", storageConfig.getBucket(), e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.bucket.check.failed",
                    e.getMessage());
        }
    }

    /**
     * 创建存储桶
     */
    private void createBucket() {
        try {
            log.info("存储桶不存在，正在创建: {}", storageConfig.getBucket());
            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(storageConfig.getBucket())
                    .build();
            s3Client.createBucket(request);
            log.info("存储桶创建成功: {}", storageConfig.getBucket());
        } catch (S3Exception e) {
            if (e.statusCode() == 409) {
                log.info("存储桶已由其他进程创建: {}", storageConfig.getBucket());
                return;
            }
            log.error("创建存储桶失败: bucket={}, error={}", storageConfig.getBucket(), e.getMessage(), e);
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "common.storage.bucket.create.failed",
                    e.getMessage());
        }
    }

    /**
     * 生成对象 Key：prefix/yyyy/MM/dd/uuid_安全文件名
     */
    private String generateFileKey(String originalFilename, String prefix) {
        String datePath = LocalDateTime.now().format(DATE_PATH_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String safeName = sanitizeFilename(originalFilename);
        return String.format("%s/%s/%s_%s", prefix, datePath, uuid, safeName);
    }

    /**
     * 清理文件名：中文转小写拼音，字母/数字保留，其余特殊字符（含 / \ 等路径分隔符）替换为下划线。
     * 示例：产品手册(终版).pdf -> chanpinshouce_zhongban_.pdf
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : filename.toCharArray()) {
            sb.append(convertChar(ch));
        }
        String safe = sb.toString();
        // 若清洗后仅剩下划线/空（如纯特殊字符文件名），回退为 unknown
        if (safe.isBlank() || safe.replace("_", "").isEmpty()) {
            return "unknown";
        }
        return safe;
    }

    /**
     * 单字符转换：中文转拼音，安全 ASCII 字符原样保留，其他替换为下划线。
     *
     * @param ch 待转换字符
     * @return 转换后的字符串片段
     */
    private String convertChar(char ch) {
        // 字母、数字、点、下划线、连字符直接保留
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                || (ch >= '0' && ch <= '9') || ch == '.' || ch == '_' || ch == '-') {
            return String.valueOf(ch);
        }
        // 汉字转小写拼音（无声调）
        if (ch >= '\u4e00' && ch <= '\u9fa5') {
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(ch, PINYIN_FORMAT);
                if (pinyins != null && pinyins.length > 0) {
                    return pinyins[0];
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                log.debug("汉字转拼音失败，按安全字符处理: {}", ch);
            }
        }
        // 其余字符（含 / \ 等）替换为下划线，避免破坏 Key 结构
        return "_";
    }
}
