package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.query.UserQueryService;
import com.silverwing.biz.iam.domain.adapter.repository.UserRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户查询服务实现（CQRS 读侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<SysUserAggregate> list(UserQuery query) {
        return userRepository.findPage(query);
    }

    @Override
    @Transactional(readOnly = true)
    public SysUserAggregate getById(Long id) {
        SysUserAggregate user = userRepository.findById(id);
        if (user != null) {
            user.clearPassword();
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }
}
