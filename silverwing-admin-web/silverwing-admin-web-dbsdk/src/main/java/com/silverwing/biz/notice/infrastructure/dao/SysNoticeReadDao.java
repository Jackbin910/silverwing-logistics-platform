package com.silverwing.biz.notice.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.silverwing.biz.notice.infrastructure.dao.po.NoticeReadUserPO;
import com.silverwing.biz.notice.infrastructure.dao.po.SysNoticePO;
import com.silverwing.biz.notice.infrastructure.dao.po.SysNoticeReadPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告已读记录数据访问对象，对应 sys_notice_read 表。
 * <p>复杂 SQL（跨表关联、批量幂等写入）统一定义在 SysNoticeReadMapper.xml 中。</p>
 */
@Mapper
public interface SysNoticeReadDao extends BaseMapper<SysNoticeReadPO> {

    /** 标记单条公告已读（唯一键冲突时忽略，保证幂等） */
    int insertNoticeRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /** 批量标记公告已读（唯一键冲突时忽略，保证幂等） */
    int insertNoticeReadBatch(@Param("userId") Long userId, @Param("noticeIds") Long[] noticeIds);

    /** 分页查询已阅读指定公告的用户列表，支持按用户名/昵称模糊筛选 */
    IPage<NoticeReadUserPO> selectReadUsersByNoticeId(IPage<NoticeReadUserPO> page,
                                                      @Param("noticeId") Long noticeId,
                                                      @Param("searchValue") String searchValue);

    /** 删除公告时清理其已读记录 */
    int deleteByNoticeIds(@Param("noticeIds") Long[] noticeIds);

    /** 查询指定用户的未读公告数量 */
    int selectUnreadCount(@Param("userId") Long userId);

    /** 查询带当前用户已读状态的公告列表（置顶展示用） */
    List<SysNoticePO> selectNoticeListWithReadStatus(@Param("userId") Long userId,
                                                     @Param("limit") int limit);
}
