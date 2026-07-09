package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.convertor.RoleConvertor;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RoleQueryService;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色查询服务实现（CQRS 读侧）
 * <p>从仓储获取聚合根后，统一经 RoleConvertor 转换为 {@link RoleResponse} 再返回。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;
    private final RoleConvertor roleConvertor;

    @Override
    @Transactional(readOnly = true)
    public PageResult<RoleResponse> list(RoleQuery query) {
        PageResult<SysRoleAggregate> page = roleRepository.findPage(query);
        List<RoleResponse> records = page.getRecords().stream()
                .map(roleConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listAllEnabled() {
        return roleRepository.findAllEnabled().stream()
                .map(roleConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        SysRoleAggregate role = roleRepository.findById(id);
        return role == null ? null : roleConvertor.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRolePermissionIds(Long roleId) {
        return roleRepository.findPermissionIdsByRoleId(roleId);
    }
}
