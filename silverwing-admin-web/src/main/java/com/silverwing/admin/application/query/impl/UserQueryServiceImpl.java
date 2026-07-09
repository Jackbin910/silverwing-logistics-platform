package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.convertor.UserConvertor;
import com.silverwing.admin.application.dto.UserResponse;
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
import java.util.stream.Collectors;

/**
 * 用户查询服务实现（CQRS 读侧）
 * <p>从仓储获取聚合根后，统一经 UserConvertor 转换为 {@link UserResponse}（不含密码与盐值）再返回。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserConvertor userConvertor;

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> list(UserQuery query) {
        PageResult<SysUserAggregate> page = userRepository.findPage(query);
        List<UserResponse> records = page.getRecords().stream()
                .map(userConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        SysUserAggregate user = userRepository.findById(id);
        return user == null ? null : userConvertor.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }
}
