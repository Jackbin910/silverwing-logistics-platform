package com.silverwing.auth.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户基础设施转换器（防腐层）
 * <p>负责 PO（AuthUserPO）与领域实体（AuthUserAggregate）之间的互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface AuthUserInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    AuthUserInfraConvertor INSTANCE = Mappers.getMapper(AuthUserInfraConvertor.class);

    /** 领域实体 -> 持久化对象 */
    AuthUserPO toPo(AuthUserAggregate user);

    /** 持久化对象 -> 领域实体 */
    AuthUserAggregate toDomain(AuthUserPO po);
}
