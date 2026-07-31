package com.silverwing.biz.post.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.post.infrastructure.dao.po.SysUserPostPO;

/**
 * 用户与岗位关联数据访问层
 * <p>仅继承 MyBatis-Plus 的 {@link BaseMapper}，所有增删改查均通过
 * {@code LambdaQueryWrapper} 在仓储实现层完成，避免散落的注解 SQL。</p>
 *
 * @author silverwing
 */
public interface SysUserPostDao extends BaseMapper<SysUserPostPO> {
}
