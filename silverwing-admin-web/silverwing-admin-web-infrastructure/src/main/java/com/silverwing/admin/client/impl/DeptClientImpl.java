package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveDeptCommand;
import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.DeptTreeResponse;
import com.silverwing.admin.application.query.DeptQuery;
import com.silverwing.admin.client.DeptClient;
import com.silverwing.admin.client.convertor.DeptConvertor;
import com.silverwing.biz.dept.domain.adapter.repository.DeptRepository;
import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.domain.service.IDeptDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门上下文防腐层适配器，串联领域服务与仓储。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptClientImpl implements DeptClient {

    private final DeptRepository deptRepository;
    private final DeptConvertor deptConvertor;
    private final IDeptDomainService deptDomainService;

    @Override
    @Transactional
    public DeptResponse create(SaveDeptCommand command) {
        SysDeptAggregate aggregate = deptConvertor.toEntity(command);
        deptDomainService.saveDept(aggregate);
        return deptConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void update(Long deptId, SaveDeptCommand command) {
        SysDeptAggregate aggregate = deptRepository.findById(deptId);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.dept.notfound");
        }
        deptConvertor.applyCommandToEntity(aggregate, command);
        deptDomainService.updateDept(aggregate);
    }

    @Override
    @Transactional
    public void delete(Long deptId) {
        deptDomainService.deleteDeptById(deptId);
    }

    @Override
    @Transactional
    public void saveSort(Long deptId, Integer orderNum) {
        deptDomainService.saveDeptSort(deptId, orderNum);
    }

    @Override
    @Transactional(readOnly = true)
    public DeptResponse getById(Long deptId) {
        SysDeptAggregate aggregate = deptRepository.findById(deptId);
        if (aggregate == null) {
            return null;
        }
        if (aggregate.getParentId() != null && aggregate.getParentId() != 0L) {
            SysDeptAggregate parent = deptRepository.findById(aggregate.getParentId());
            aggregate.setParentName(parent == null ? null : parent.getDeptName());
        }
        return deptConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptResponse> list(DeptQuery query) {
        com.silverwing.biz.dept.domain.model.query.DeptQuery domainQuery =
                new com.silverwing.biz.dept.domain.model.query.DeptQuery();
        domainQuery.setDeptName(query.getDeptName());
        domainQuery.setStatus(query.getStatus());
        return deptDomainService.findList(domainQuery).stream()
                .map(deptConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptResponse> listExcludeChild(Long deptId) {
        return deptDomainService.findAll().stream()
                .filter(d -> !isSelfOrDescendant(d, deptId))
                .map(deptConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptTreeResponse> treeSelect() {
        List<DeptResponse> all = deptDomainService.findAll().stream()
                .map(deptConvertor::toResponse)
                .collect(Collectors.toList());
        return buildTree(all);
    }

    @Override
    @Transactional(readOnly = true)
    public DeptRoleTreeResponse roleDeptTreeSelect(Long roleId) {
        DeptRoleTreeResponse response = new DeptRoleTreeResponse();
        response.setDepts(treeSelect());
        response.setCheckedKeys(deptDomainService.findRoleDeptIds(roleId));
        return response;
    }

    private boolean isSelfOrDescendant(SysDeptAggregate dept, Long deptId) {
        if (dept.getDeptId().equals(deptId)) {
            return true;
        }
        if (dept.getAncestors() != null) {
            for (String id : dept.getAncestors().split(",")) {
                if (id.equals(String.valueOf(deptId))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<DeptTreeResponse> buildTree(List<DeptResponse> list) {
        Map<Long, DeptTreeResponse> nodeMap = new HashMap<>();
        List<DeptTreeResponse> roots = new ArrayList<>();
        for (DeptResponse resp : list) {
            nodeMap.put(resp.getDeptId(), new DeptTreeResponse(resp.getDeptId(), resp.getDeptName()));
        }
        for (DeptResponse resp : list) {
            DeptTreeResponse node = nodeMap.get(resp.getDeptId());
            if (resp.getParentId() == null || resp.getParentId() == 0L
                    || !nodeMap.containsKey(resp.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(resp.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }
}
