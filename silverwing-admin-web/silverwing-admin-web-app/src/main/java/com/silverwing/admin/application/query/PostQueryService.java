package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 岗位查询服务（CQRS 读侧）
 */
public interface PostQueryService {

    PageResult<PostResponse> list(PostPageQuery query);

    List<PostResponse> listAll();

    PostResponse getById(Long id);

    List<PostResponse> optionSelect();

    /**
     * 导出查询全部岗位
     */
    List<PostResponse> exportList();
}
