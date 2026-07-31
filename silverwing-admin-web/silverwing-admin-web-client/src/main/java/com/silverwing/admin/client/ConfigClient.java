package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveConfigCommand;
import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.admin.application.query.ConfigPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 参数配置上下文防腐层端口。
 * <p>应用层通过该端口访问 biz-config 参数配置上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface ConfigClient {

    /** 创建参数配置 */
    ConfigResponse create(SaveConfigCommand command);

    /** 更新参数配置 */
    void update(Long id, SaveConfigCommand command);

    /** 批量删除参数配置 */
    void deleteByIds(Long[] ids);

    /** 分页查询参数配置 */
    PageResult<ConfigResponse> list(ConfigPageQuery query);

    /** 查询全部参数配置（用于导出） */
    List<ConfigResponse> listExport(ConfigPageQuery query);

    /** 根据主键查询参数配置 */
    ConfigResponse getById(Long id);

    /** 根据参数键名查询参数值 */
    String getByKey(String configKey);

    /** 刷新参数配置缓存 */
    void refreshCache();
}
