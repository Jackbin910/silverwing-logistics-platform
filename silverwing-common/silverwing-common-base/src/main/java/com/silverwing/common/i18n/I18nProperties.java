package com.silverwing.common.i18n;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 国际化配置属性
 * <p>各微服务可通过 application.yml 中的 silverwing.i18n.extra-basenames 扩展专属文案</p>
 * <p>配置示例：</p>
 * <pre>
 * silverwing:
 *   i18n:
 *     extra-basenames:
 *       - i18n/service
 * </pre>
 *
 * @author silverwing
 */
@Data
@ConfigurationProperties(prefix = "silverwing.i18n")
public class I18nProperties {

    /** 微服务额外的 basename 列表，用于加载业务专属文案 */
    private List<String> extraBasenames = new ArrayList<>();

    /** 缓存秒数，-1 表示永久缓存，开发环境可设小值 */
    private int cacheSeconds = 3600;
}
