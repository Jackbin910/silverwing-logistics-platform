package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.admin.client.DictDataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 字典数据命令服务。
 */
@Service
@RequiredArgsConstructor
public class DictDataCommandService {

    private final DictDataClient dictDataClient;

    /** 新增字典数据 */
    public DictDataResponse create(SaveDictDataCommand command) {
        return dictDataClient.create(command);
    }

    /** 修改字典数据 */
    public void update(Long id, SaveDictDataCommand command) {
        dictDataClient.update(id, command);
    }

    /** 批量删除字典数据 */
    public void delete(Long[] ids) {
        dictDataClient.deleteByIds(ids);
    }

    /** 删除单条字典数据 */
    public void delete(Long id) {
        dictDataClient.deleteByIds(new Long[]{id});
    }
}
