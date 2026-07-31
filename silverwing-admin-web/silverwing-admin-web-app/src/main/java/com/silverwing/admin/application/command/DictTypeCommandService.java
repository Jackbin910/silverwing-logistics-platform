package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.admin.client.DictTypeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 字典类型命令服务。
 */
@Service
@RequiredArgsConstructor
public class DictTypeCommandService {

    private final DictTypeClient dictTypeClient;

    /** 新增字典类型 */
    public DictTypeResponse create(SaveDictTypeCommand command) {
        return dictTypeClient.create(command);
    }

    /** 修改字典类型 */
    public void update(Long id, SaveDictTypeCommand command) {
        dictTypeClient.update(id, command);
    }

    /** 批量删除字典类型 */
    public void delete(Long[] ids) {
        dictTypeClient.deleteByIds(ids);
    }
}
