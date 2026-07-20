package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthUserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问对象（MyBatis-Plus）
 */
@Mapper
public interface AuthUserDao extends BaseMapper<AuthUserPO> {
}
