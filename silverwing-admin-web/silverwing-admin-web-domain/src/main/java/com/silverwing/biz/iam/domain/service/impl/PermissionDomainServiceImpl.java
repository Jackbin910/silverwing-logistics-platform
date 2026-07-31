package com.silverwing.biz.iam.domain.service.impl;

import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.service.IPermissionDomainService;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionDomainServiceImpl implements IPermissionDomainService {

    private final PermissionRepository permissionRepository;

    @Override
    public SysPermissionAggregate save(SysPermissionAggregate permission) {
        permissionRepository.save(permission);
        return permission;
    }

    @Override
    public void deleteById(Long id) {
        // 存在子级菜单时不允许删除
        if (permissionRepository.hasChildByParentId(id)) {
            throw new BusinessException("admin.permission.hasChild");
        }
        // 权限已分配给角色时不允许删除
        if (permissionRepository.countByRoleId(id) > 0) {
            throw new BusinessException("admin.permission.assigned");
        }
        permissionRepository.deleteById(id);
    }

    @Override
    public void checkPermissionNameUnique(Long id, Long parentId, String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            return;
        }
        SysPermissionAggregate exist = permissionRepository.findByNameAndParent(parentId, permissionName);
        if (exist != null && !exist.getId().equals(id)) {
            throw new BusinessException("admin.permission.name.exists");
        }
    }

    @Override
    public void checkRouteConfigUnique(Long id, String routeName) {
        if (routeName == null || routeName.isBlank()) {
            return;
        }
        SysPermissionAggregate exist = permissionRepository.findByRouteName(routeName);
        if (exist != null && !exist.getId().equals(id)) {
            throw new BusinessException("admin.permission.route.exists");
        }
    }

    @Override
    public void updateSort(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        permissionRepository.updateSort(ids);
    }
}
