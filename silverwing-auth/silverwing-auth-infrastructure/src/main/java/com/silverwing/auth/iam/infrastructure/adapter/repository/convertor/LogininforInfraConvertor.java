package com.silverwing.auth.iam.infrastructure.adapter.repository.convertor;

import com.silverwing.auth.iam.domain.model.aggregate.LogininforAggregate;
import com.silverwing.auth.iam.infrastructure.dao.po.SysLogininforPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 登录日志 PO 与聚合根互转（基础设施层内部使用，采用 INSTANCE 静态实例）。
 *
 * @author silverwing
 */
@Mapper
public interface LogininforInfraConvertor {

    LogininforInfraConvertor INSTANCE = Mappers.getMapper(LogininforInfraConvertor.class);

    /**
     * 聚合根转换为 PO。
     *
     * @param aggregate 聚合根
     * @return 持久化对象
     */
    SysLogininforPO toPo(LogininforAggregate aggregate);
}
