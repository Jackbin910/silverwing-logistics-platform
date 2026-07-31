package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.client.DeptClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 部门命令服务，编排部门新增/修改/删除/排序命令。
 */
@Service
@RequiredArgsConstructor
public class DeptCommandService {

    private final DeptClient deptClient;

    /** 新增部门 */
    public DeptResponse create(SaveDeptCommand command) {
        return deptClient.create(command);
    }

    /** 修改部门 */
    public void update(Long deptId, SaveDeptCommand command) {
        deptClient.update(deptId, command);
    }

    /** 删除部门 */
    public void delete(Long deptId) {
        deptClient.delete(deptId);
    }

    /** 保存部门排序 */
    public void saveSort(Long deptId, Integer orderNum) {
        deptClient.saveSort(deptId, orderNum);
    }
}
