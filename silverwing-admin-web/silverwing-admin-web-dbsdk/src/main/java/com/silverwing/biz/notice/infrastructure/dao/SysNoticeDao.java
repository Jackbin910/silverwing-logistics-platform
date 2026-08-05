package com.silverwing.biz.notice.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.notice.infrastructure.dao.po.SysNoticePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告数据访问对象，对应 sys_notice 表。
 */
@Mapper
public interface SysNoticeDao extends BaseMapper<SysNoticePO> {
}
