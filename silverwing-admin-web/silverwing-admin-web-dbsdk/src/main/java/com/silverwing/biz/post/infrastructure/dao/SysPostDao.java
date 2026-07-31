package com.silverwing.biz.post.infrastructure.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.silverwing.biz.post.infrastructure.dao.po.SysPostPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位数据访问层
 * <p>复杂查询（含关联用户数子查询）的实现见对应的 XML Mapper（SysPostMapper.xml）。</p>
 *
 * @author silverwing
 */
public interface SysPostDao extends BaseMapper<SysPostPO> {

    /**
     * 分页查询岗位（含关联用户数）
     *
     * @param wrapper 查询条件
     * @return 岗位列表（含 user_count）
     */
    List<SysPostPO> selectPostList(@Param(Constants.WRAPPER) Wrapper<SysPostPO> wrapper);

    /**
     * 导出查询全部岗位（含关联用户数）
     *
     * @param wrapper 查询条件
     * @return 岗位列表（含 user_count）
     */
    List<SysPostPO> selectPostAll(@Param(Constants.WRAPPER) Wrapper<SysPostPO> wrapper);
}
