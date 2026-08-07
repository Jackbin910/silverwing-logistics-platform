package com.silverwing.ai.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音识别（ASR）外部服务客户端
 * <p>
 * 封装对本地部署语音模型服务的调用（FastAPI：POST /asr，multipart 上传 audio 字段）。
 * 服务地址通过配置注入，不在 Nacos 注册，故使用 RestTemplate 直接调用，不依赖服务发现。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Component
public class AsrClient {

    private final RestTemplate restTemplate;

    @Value("${asr.base-url:http://192.168.31.84:8000}")
    private String baseUrl;

    @Value("${asr.path:/asr}")
    private String path;

    public AsrClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 将音频文件转换为文字
     *
     * @param audio 上传的音频文件（multipart 字段名需为 audio）
     * @return 识别出的文字；调用失败或空结果返回 null
     */
    public String recognize(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            log.warn("ASR 识别失败：音频文件为空");
            return null;
        }
        String url = baseUrl + path;
        try {
            // 构造 multipart 请求体，字段名 audio 与语音服务接口一致
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", new MultipartInputStreamFileResource(audio));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                response.getBody();
                return extractText(response.getBody());
            }
            log.error("ASR 识别返回非成功状态：{}，body={}", response.getStatusCode(), response.getBody());
            return null;
        } catch (Exception e) {
            log.error("ASR 识别调用异常，url={}：{}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 从响应体中提取文字
     * <p>兼容两种返回：纯字符串 或 JSON 对象（优先取 text / result 字段）。</p>
     *
     * @param body 响应字符串
     * @return 识别文字
     */
        private String extractText(String body) {
            String trimmed = body.trim();
            if (JSONUtil.isTypeJSON(trimmed)) {
                JSONObject json = JSONUtil.parseObj(trimmed);
                // 优先取常见字段，命中即返回
                for (String key : new String[]{"text", "result", "transcript", "content"}) {
                    if (json.containsKey(key) && json.getStr(key) != null) {
                        return json.getStr(key);
                    }
                }
                // 无已知字段则退回原始字符串
                return trimmed;
            }
            return trimmed;
        }
}
