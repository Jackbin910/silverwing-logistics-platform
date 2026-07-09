package com.silverwing.biz.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.infrastructure.dao.po.SysRolePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色基础设施转换器（防腐层）
 * <p>负责 PO（SysRolePO）与领域实体（SysRoleAggregate）之间的互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface RoleInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    RoleInfraConvertor INSTANCE = Mappers.getMapper(RoleInfraConvertor.class);

    /**
     * 领域实体 -> 持久化对象
     */
    SysRolePO toPo(SysRoleAggregate role);

    /**
     * 持久化对象 -> 领域实体
     */
    SysRoleAggregate toDomain(SysRolePO po);
}
