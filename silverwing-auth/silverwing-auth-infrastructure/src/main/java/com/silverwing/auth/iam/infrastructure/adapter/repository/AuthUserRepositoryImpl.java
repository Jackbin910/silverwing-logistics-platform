package com.silverwing.auth.iam.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.auth.iam.domain.adapter.repository.UserRepository;
import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;
import com.silverwing.auth.iam.infrastructure.adapter.repository.convertor.AuthUserInfraConvertor;
import com.silverwing.auth.iam.infrastructure.dao.AuthUserDao;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现（基础设施适配器）
 * <p>通过 DAO 操作 PO，并经 AuthUserInfraConvertor 与领域实体互转。</p>
 */
@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryImpl implements UserRepository {

    private final AuthUserDao authUserDao;

    @Override
    public AuthUserAggregate findByUsername(String username) {
        LambdaQueryWrapper<AuthUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthUserPO::getUsername, username);
        return AuthUserInfraConvertor.INSTANCE.toDomain(authUserDao.selectOne(wrapper));
    }

    @Override
    public AuthUserAggregate findById(Long id) {
        return AuthUserInfraConvertor.INSTANCE.toDomain(authUserDao.selectById(id));
    }
}
