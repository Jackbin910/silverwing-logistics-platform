package com.silverwing.biz.post.domain.service;

import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;

import java.util.List;

/**
 * 岗位领域服务
 *
 * @author silverwing
 */
public interface IPostDomainService {

    /**
     * 分页查询岗位
     */
    com.silverwing.common.domain.PageResult<SysPostAggregate> selectPostPage(
            com.silverwing.biz.post.domain.model.query.PostQuery query, long current, long size);

    /**
     * 查询全部岗位（导出）
     */
    List<SysPostAggregate> selectPostAll(com.silverwing.biz.post.domain.model.query.PostQuery query);

    /**
     * 根据ID查询岗位
     */
    SysPostAggregate selectPostById(Long postId);

    /**
     * 新增岗位（校验编码/名称唯一性）
     */
    void insertPost(SysPostAggregate post);

    /**
     * 更新岗位（校验编码/名称唯一性，排除自身）
     */
    void updatePost(SysPostAggregate post);

    /**
     * 批量删除岗位（含级联用户关联清理）
     */
    void deletePostByIds(List<Long> ids);

    /**
     * 查询用户关联的岗位ID集合
     *
     * @param userId 用户ID
     * @return 岗位ID集合
     */
    List<Long> getUserPostIds(Long userId);

    /**
     * 全量覆盖用户岗位关联（先删后插）
     *
     * @param userId  用户ID
     * @param postIds 岗位ID集合
     */
    void assignPosts(Long userId, List<Long> postIds);
}
