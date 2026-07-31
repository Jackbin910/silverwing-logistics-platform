package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SavePostCommand;
import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.application.query.PostPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * Post 子域防腐层端口（依赖倒置）
 *
 * @author silverwing
 */
public interface IamPostClient {

    /**
     * 新增岗位
     */
    PostResponse create(SavePostCommand command);

    /**
     * 更新岗位
     */
    void update(Long id, SavePostCommand command);

    /**
     * 删除岗位（支持批量）
     */
    void delete(List<Long> ids);

    /**
     * 分页查询岗位
     */
    PageResult<PostResponse> list(PostPageQuery query);

    /**
     * 查询全部岗位（导出）
     */
    List<PostResponse> listAll();

    /**
     * 根据ID查询岗位
     */
    PostResponse getById(Long id);

    /**
     * 查询岗位选项（全部正常岗位）
     */
    List<PostResponse> optionSelect();

    /**
     * 查询用户关联的岗位ID集合
     *
     * @param userId 用户ID
     * @return 岗位ID集合
     */
    List<Long> getUserPostIds(Long userId);

    /**
     * 全量覆盖用户岗位关联
     *
     * @param userId  用户ID
     * @param postIds 岗位ID集合
     */
    void assignPosts(Long userId, List<Long> postIds);
}
