package com.silverwing.biz.notice.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;
import com.silverwing.biz.notice.infrastructure.dao.po.SysNoticePO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.nio.charset.StandardCharsets;

/**
 * 通知公告 PO 与聚合根互转。
 * <p>sys_notice.notice_content 为 longblob 类型，需在字节数组与字符串之间做 UTF-8 编解码。</p>
 */
@Mapper
public interface NoticeInfraConvertor {

    NoticeInfraConvertor INSTANCE = Mappers.getMapper(NoticeInfraConvertor.class);

    /** 聚合根转 PO，公告内容按 UTF-8 编码为字节数组 */
    @Mapping(target = "noticeContent", source = "noticeContent", qualifiedByName = "contentToBytes")
    SysNoticePO toPo(SysNoticeAggregate aggregate);

    /** PO 转聚合根，公告内容按 UTF-8 解码为字符串 */
    @Mapping(target = "noticeContent", source = "noticeContent", qualifiedByName = "contentToString")
    SysNoticeAggregate toDomain(SysNoticePO po);

    /** 公告内容字符串编码为字节数组 */
    @Named("contentToBytes")
    static byte[] contentToBytes(String content) {
        return content == null ? null : content.getBytes(StandardCharsets.UTF_8);
    }

    /** 公告内容字节数组解码为字符串 */
    @Named("contentToString")
    static String contentToString(byte[] content) {
        return content == null ? null : new String(content, StandardCharsets.UTF_8);
    }
}
