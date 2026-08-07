package com.silverwing.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 配置
 * <p>用于调用未注册到 Nacos 的外部 HTTP 服务（如本地语音识别模型）。
 * 设置连接与读取超时，避免长语音识别阻塞调用线程。</p>
 *
 * @author silverwing
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 语音识别专用 RestTemplate
     * <p>连接超时 5s，读取超时 60s（语音模型推理耗时较长）。</p>
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate asrRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return new RestTemplate(factory);
    }
}
