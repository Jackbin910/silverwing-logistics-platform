package com.silverwing.biz.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.infrastructure.dao.po.SysPermissionPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 权限基础设施转换器（防腐层）
 * <p>负责 PO（SysPermissionPO）与领域实体（SysPermissionAggregate）之间的互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface PermissionInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    PermissionInfraConvertor INSTANCE = Mappers.getMapper(PermissionInfraConvertor.class);

    /**
     * 领域实体 -> 持久化对象
     */
    SysPermissionPO toPo(SysPermissionAggregate permission);

    /**
     * 持久化对象 -> 领域实体
     */
    SysPermissionAggregate toDomain(SysPermissionPO po);
}
