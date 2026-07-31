package com.silverwing.biz.post.domain.adapter.repository;

import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;
import com.silverwing.biz.post.domain.model.query.PostQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 岗位仓储端口（依赖倒置）
 *
 * @author silverwing
 */
public interface PostRepository {

    /**
     * 分页查询岗位
     */
    PageResult<SysPostAggregate> selectPostPage(PostQuery query, long current, long size);

    /**
     * 查询全部岗位（导出）
     */
    List<SysPostAggregate> selectPostAll(PostQuery query);

    /**
     * 根据ID查询岗位
     */
    SysPostAggregate selectPostById(Long postId);

    /**
     * 校验岗位编码唯一性（排除指定ID）
     */
    boolean checkPostCodeUnique(String postCode, Long excludeId);

    /**
     * 校验岗位名称唯一性（排除指定ID）
     */
    boolean checkPostNameUnique(String postName, Long excludeId);

    /**
     * 保存（新增/更新）
     */
    void save(SysPostAggregate post);

    /**
     * 批量删除（含级联删除用户关联）
     */
    void deleteByIds(List<Long> ids);

    /**
     * 统计岗位下关联用户数
     */
    Long countUserByPostId(Long postId);

    /**
     * 查询某用户关联的岗位ID集合
     */
    List<Long> selectPostIdsByUserId(Long userId);

    /**
     * 全量覆盖用户岗位关联（先删后插）
     *
     * @param userId  用户ID
     * @param postIds 岗位ID集合
     */
    void saveUserPosts(Long userId, List<Long> postIds);

    /**
     * 删除用户全部岗位关联
     *
     * @param userId 用户ID
     */
    void deleteUserPosts(Long userId);
}
