package com.silverwing.admin.application.command;

import com.silverwing.admin.application.convertor.RoleConvertor;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.service.IRoleDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色命令服务（CQRS 写侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final RoleConvertor roleConvertor;
    private final IRoleDomainService roleDomainService;

    @Transactional
    public RoleResponse create(SaveRoleCommand command) {
        SysRoleAggregate role = roleConvertor.toEntity(command);
        // 领域服务负责角色编码唯一性校验与持久化
        role = roleDomainService.registerRole(role);
        log.info("新建角色成功 roleCode={}, id={}", role.getRoleCode(), role.getId());
        return roleConvertor.toResponse(role);
    }

    @Transactional
    public void update(Long id, SaveRoleCommand command) {
        SysRoleAggregate role = roleRepository.findById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
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

    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除（含级联清理）
        roleDomainService.deleteById(id);
        log.info("删除角色 id={}, 已级联清理关联数据", id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 领域服务负责权限全量分配
        roleDomainService.assignPermissions(roleId, permissionIds);
        log.info("分配角色权限 roleId={}, 权限数={}", roleId,
                permissionIds == null ? 0 : permissionIds.size());
    }
}
