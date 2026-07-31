package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.DeptTreeResponse;
import com.silverwing.admin.application.query.DeptQuery;
import com.silverwing.admin.application.query.DeptQueryService;
import com.silverwing.admin.client.DeptClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门查询服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptQueryServiceImpl implements DeptQueryService {

    private final DeptClient deptClient;

    @Override
    public DeptResponse getById(Long deptId) {
        return deptClient.getById(deptId);
    }

    @Override
    public List<DeptResponse> list(DeptQuery query) {
        return deptClient.list(query);
    }

    @Override
    public List<DeptResponse> listExcludeChild(Long deptId) {
        return deptClient.listExcludeChild(deptId);
    }

    @Override
    public List<DeptTreeResponse> treeSelect() {
        return deptClient.treeSelect();
    }

    @Override
    public DeptRoleTreeResponse roleDeptTreeSelect(Long roleId) {
        return deptClient.roleDeptTreeSelect(roleId);
    }
}
