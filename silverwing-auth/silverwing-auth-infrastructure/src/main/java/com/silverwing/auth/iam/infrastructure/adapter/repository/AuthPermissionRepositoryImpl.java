package com.silverwing.auth.iam.infrastructure.adapter.repository;

import com.silverwing.auth.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.auth.iam.infrastructure.dao.AuthPermissionDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限仓储实现（基础设施适配器）
 */
@Repository
@RequiredArgsConstructor
public class AuthPermissionRepositoryImpl implements PermissionRepository {

    private final AuthPermissionDao authPermissionDao;

    @Override
    public List<String> findPermissionCodesByUserId(Long userId) {
        return authPermissionDao.selectPermissionCodesByUserId(userId);
    }
}
