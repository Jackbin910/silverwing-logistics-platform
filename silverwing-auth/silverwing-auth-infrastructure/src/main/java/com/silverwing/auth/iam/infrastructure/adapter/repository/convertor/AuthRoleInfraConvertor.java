package com.silverwing.auth.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.auth.iam.domain.model.aggregate.AuthRoleAggregate;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthRolePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色基础设施转换器（防腐层）
 * <p>负责 PO（AuthRolePO）与领域实体（AuthRoleAggregate）之间的互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface AuthRoleInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    AuthRoleInfraConvertor INSTANCE = Mappers.getMapper(AuthRoleInfraConvertor.class);

    /** 领域实体 -> 持久化对象 */
    AuthRolePO toPo(AuthRoleAggregate role);

    /** 持久化对象 -> 领域实体 */
    AuthRoleAggregate toDomain(AuthRolePO po);
}
