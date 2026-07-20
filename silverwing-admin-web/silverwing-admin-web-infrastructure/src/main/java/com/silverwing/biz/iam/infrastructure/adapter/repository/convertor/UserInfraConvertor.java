package com.silverwing.biz.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户基础设施转换器（防腐层）
 * <p>
 * 负责 PO（SysUserPO）与领域实体（SysUserAggregate）之间的互转。
 * 以静态 {@code INSTANCE} 调用，不依赖 Spring 容器。
 * </p>
 */
@Mapper
public interface UserInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    UserInfraConvertor INSTANCE = Mappers.getMapper(UserInfraConvertor.class);

    /**
     * 领域实体 -> 持久化对象
     */
    SysUserPO toPo(SysUserAggregate user);

    /**
     * 持久化对象 -> 领域实体
     */
    SysUserAggregate toDomain(SysUserPO po);
}
