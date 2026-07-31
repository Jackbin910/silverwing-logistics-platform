package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.admin.client.ConfigClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 参数配置命令服务。
 */
@Service
@RequiredArgsConstructor
public class ConfigCommandService {

    private final ConfigClient configClient;

    /** 新增参数配置 */
    public ConfigResponse create(SaveConfigCommand command) {
        return configClient.create(command);
    }

    /** 修改参数配置 */
    public void update(Long id, SaveConfigCommand command) {
        configClient.update(id, command);
    }

    /** 批量删除参数配置 */
    public void delete(Long[] ids) {
        configClient.deleteByIds(ids);
    }

    /** 刷新参数配置缓存 */
    public void refreshCache() {
        configClient.refreshCache();
    }
}
