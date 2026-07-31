package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典数据查询服务。
 */
public interface DictDataQueryService {

    /** 分页查询 */
    PageResult<DictDataResponse> list(DictDataPageQuery query);

    /** 查询全部（用于导出） */
    List<DictDataResponse> listExport(DictDataPageQuery query);

    /** 根据主键查询 */
    DictDataResponse getById(Long id);

    /** 根据字典类型查询 */
    List<DictDataResponse> getByDictType(String dictType);
}
