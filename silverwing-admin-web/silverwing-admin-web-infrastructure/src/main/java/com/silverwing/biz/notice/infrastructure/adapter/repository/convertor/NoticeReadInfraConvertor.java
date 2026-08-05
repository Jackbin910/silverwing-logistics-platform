package com.silverwing.biz.notice.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.notice.domain.model.entity.NoticeReadUserEntity;
import com.silverwing.biz.notice.infrastructure.dao.po.NoticeReadUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 公告已读用户 PO 与领域模型互转。
 */
@Mapper
public interface NoticeReadInfraConvertor {

    NoticeReadInfraConvertor INSTANCE = Mappers.getMapper(NoticeReadInfraConvertor.class);

    /** PO 转领域模型 */
    NoticeReadUserEntity toDomain(NoticeReadUserPO po);
}
