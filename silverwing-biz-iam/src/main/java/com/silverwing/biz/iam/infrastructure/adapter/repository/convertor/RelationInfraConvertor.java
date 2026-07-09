package com.silverwing.biz.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.iam.domain.model.entity.SysRolePermission;
import com.silverwing.biz.iam.domain.model.entity.SysUserRole;
import com.silverwing.biz.iam.infrastructure.dao.po.SysRolePermissionPO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserRolePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 关联表基础设施转换器（防腐层）
 * <p>负责用户-角色、角色-权限两张关联表的 PO 与领域实体互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface RelationInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    RelationInfraConvertor INSTANCE = Mappers.getMapper(RelationInfraConvertor.class);

    SysUserRolePO toUserRolePo(SysUserRole domain);

    SysUserRole toUserRoleDomain(SysUserRolePO po);

    SysRolePermissionPO toRolePermissionPo(SysRolePermission domain);

    SysRolePermission toRolePermissionDomain(SysRolePermissionPO po);
}
