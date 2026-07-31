package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveDictTypeCommand;
import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.admin.application.query.DictTypePageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典类型上下文防腐层端口。
 * <p>应用层通过该端口访问 biz-dict 字典类型上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface DictTypeClient {

    /** 创建字典类型 */
    DictTypeResponse create(SaveDictTypeCommand command);

    /** 更新字典类型 */
    void update(Long id, SaveDictTypeCommand command);

    /** 批量删除字典类型 */
    void deleteByIds(Long[] ids);

    /** 分页查询字典类型 */
    PageResult<DictTypeResponse> list(DictTypePageQuery query);

    /** 查询全部字典类型（用于导出） */
    List<DictTypeResponse> listExport(DictTypePageQuery query);

    /** 根据主键查询字典类型 */
    DictTypeResponse getById(Long id);

    /** 查询全部字典类型（下拉选项） */
    List<DictTypeResponse> optionSelect();

    /** 刷新字典缓存 */
    void refreshCache();
}
