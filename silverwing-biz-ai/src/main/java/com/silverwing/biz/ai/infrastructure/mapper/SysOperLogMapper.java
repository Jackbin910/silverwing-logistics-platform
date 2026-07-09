package com.silverwing.biz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.ai.infrastructure.dao.po.SysOperLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLogPO> {

    /**
     * 批量插入操作日志
     *
     * @param list 操作日志 PO 列表
     */
    void insertBatch(@Param("list") List<SysOperLogPO> list);
}
