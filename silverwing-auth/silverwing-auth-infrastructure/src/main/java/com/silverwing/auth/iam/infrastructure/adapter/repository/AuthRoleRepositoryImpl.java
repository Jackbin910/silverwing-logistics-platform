package com.silverwing.auth.iam.infrastructure.adapter.repository;

import com.silverwing.auth.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.auth.iam.domain.model.aggregate.AuthRoleAggregate;
import com.silverwing.auth.iam.infrastructure.adapter.repository.convertor.AuthRoleInfraConvertor;
import com.silverwing.auth.iam.infrastructure.dao.AuthRoleDao;
import com.silverwing.auth.iam.infrastructure.dao.AuthUserRoleDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色仓储实现（基础设施适配器）
 */
@Repository
@RequiredArgsConstructor
public class AuthRoleRepositoryImpl implements RoleRepository {

    private final AuthUserRoleDao authUserRoleDao;
    private final AuthRoleDao authRoleDao;

    @Override
    public List<AuthRoleAggregate> findRolesByUserId(Long userId) {
        return authUserRoleDao.selectRolesByUserId(userId).stream()
                .map(AuthRoleInfraConvertor.INSTANCE::toDomain)
                .toList();
    }
}
