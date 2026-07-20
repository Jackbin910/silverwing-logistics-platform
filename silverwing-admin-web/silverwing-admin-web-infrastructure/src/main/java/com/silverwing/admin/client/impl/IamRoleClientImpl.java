package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.admin.client.IamRoleClient;
import com.silverwing.admin.client.convertor.RoleConvertor;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.biz.iam.domain.service.IRoleDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IAM 角色上下文防腐层适配器
 * <p>本类是唯一直接依赖 biz-iam 角色领域层（聚合根、仓储、领域服务）的地方。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamRoleClientImpl implements IamRoleClient {

    private final RoleRepository roleRepository;
    private final RoleConvertor roleConvertor;
    private final IRoleDomainService roleDomainService;

    @Override
    @Transactional
    public RoleResponse create(SaveRoleCommand command) {
        SysRoleAggregate role = roleConvertor.toEntity(command);
        // 领域服务负责角色编码唯一性校验与持久化
        role = roleDomainService.registerRole(role);
        log.info("新建角色成功 roleCode={}, id={}", role.getRoleCode(), role.getId());
        return roleConvertor.toResponse(role);
    }

    @Override
    @Transactional
    public void update(Long id, SaveRoleCommand command) {
        SysRoleAggregate role = roleRepository.findById(id);
        if (role == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.role.notfound");
        }
        if (command.getRoleCode() != null) {
            role.setRoleCode(command.getRoleCode());
        }
        if (command.getRoleName() != null) {
            role.setRoleName(command.getRoleName());
        }
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) {
                role.enable();
            } else {
                role.disable();
            }
        }
        // 领域服务负责持久化
        roleDomainService.update(role);
        log.info("更新角色 id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除（含级联清理）
        roleDomainService.deleteById(id);
        log.info("删除角色 id={}, 已级联清理关联数据", id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 领域服务负责权限全量分配
        roleDomainService.assignPermissions(roleId, permissionIds);
        log.info("分配角色权限 roleId={}, 权限数={}", roleId,
                permissionIds == null ? 0 : permissionIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RoleResponse> list(RolePageQuery query) {
        RoleQuery roleQuery = toRoleQuery(query);
        PageResult<SysRoleAggregate> page = roleRepository.findPage(roleQuery);
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

    /**
     * 将本模块分页查询条件翻译为 biz-iam 领域查询对象
     */
    private RoleQuery toRoleQuery(RolePageQuery query) {
        RoleQuery roleQuery = new RoleQuery();
        roleQuery.setCurrent(query.getCurrent());
        roleQuery.setSize(query.getSize());
        roleQuery.setRoleName(query.getRoleName());
        roleQuery.setStatus(query.getStatus());
        return roleQuery;
    }
}
