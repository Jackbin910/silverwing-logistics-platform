package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 参数配置查询服务。
 */
public interface ConfigQueryService {

    /** 分页查询 */
    PageResult<ConfigResponse> list(ConfigPageQuery query);

    /** 导出列表查询 */
    List<ConfigResponse> listExport(ConfigPageQuery query);

    /** 详情 */
    ConfigResponse getById(Long id);

    /** 根据参数键名查询参数值 */
    String getByKey(String configKey);
}
