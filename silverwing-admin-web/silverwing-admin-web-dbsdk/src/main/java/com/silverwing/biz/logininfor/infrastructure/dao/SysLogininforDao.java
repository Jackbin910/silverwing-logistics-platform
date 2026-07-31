package com.silverwing.biz.logininfor.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问记录数据访问层。
 */
@Mapper
public interface SysLogininforDao extends BaseMapper<SysLogininforPO> {

    /**
     * 清空登录日志（物理删除全部记录并重置自增）。
     */
    @Delete("TRUNCATE TABLE sys_logininfor")
    void cleanLogininfor();
}
