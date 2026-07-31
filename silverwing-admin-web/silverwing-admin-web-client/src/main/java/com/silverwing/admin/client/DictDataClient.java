package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveDictDataCommand;
import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.admin.application.query.DictDataPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典数据上下文防腐层端口。
 * <p>应用层通过该端口访问 biz-dict 字典数据上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface DictDataClient {

    /** 创建字典数据 */
    DictDataResponse create(SaveDictDataCommand command);

    /** 更新字典数据 */
    void update(Long id, SaveDictDataCommand command);

    /** 批量删除字典数据 */
    void deleteByIds(Long[] ids);

    /** 分页查询字典数据 */
    PageResult<DictDataResponse> list(DictDataPageQuery query);

    /** 查询全部字典数据（用于导出） */
    List<DictDataResponse> listExport(DictDataPageQuery query);

    /** 根据主键查询字典数据 */
    DictDataResponse getById(Long id);

    /** 根据字典类型查询字典数据（带缓存） */
    List<DictDataResponse> getByDictType(String dictType);
}
