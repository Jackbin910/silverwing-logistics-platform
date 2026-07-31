package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.application.query.PostPageQuery;
import com.silverwing.admin.application.query.PostQueryService;
import com.silverwing.admin.client.IamPostClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位查询服务实现（CQRS 读侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final IamPostClient iamPostClient;

    @Override
    public PageResult<PostResponse> list(PostPageQuery query) {
        return iamPostClient.list(query);
    }

    @Override
    public List<PostResponse> listAll() {
        return iamPostClient.listAll();
    }

    @Override
    public PostResponse getById(Long id) {
        return iamPostClient.getById(id);
    }

    @Override
    public List<PostResponse> optionSelect() {
        return iamPostClient.optionSelect();
    }

    @Override
    public List<PostResponse> exportList() {
        return iamPostClient.listAll();
    }
}
