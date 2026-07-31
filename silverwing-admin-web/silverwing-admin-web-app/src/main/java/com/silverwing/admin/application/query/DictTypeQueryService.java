package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典类型查询服务。
 */
public interface DictTypeQueryService {

    /** 分页查询 */
    PageResult<DictTypeResponse> list(DictTypePageQuery query);

    /** 查询全部（用于导出） */
    List<DictTypeResponse> listExport(DictTypePageQuery query);

    /** 根据主键查询 */
    DictTypeResponse getById(Long id);

    /** 下拉选项 */
    List<DictTypeResponse> optionSelect();

    /** 刷新字典缓存 */
    void refreshCache();
}
