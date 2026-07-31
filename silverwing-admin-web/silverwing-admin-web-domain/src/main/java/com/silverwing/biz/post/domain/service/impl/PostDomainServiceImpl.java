package com.silverwing.biz.post.domain.service.impl;

import com.silverwing.biz.post.domain.adapter.repository.PostRepository;
import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;
import com.silverwing.biz.post.domain.model.query.PostQuery;
import com.silverwing.biz.post.domain.service.IPostDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位领域服务实现
 *
 * @author silverwing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostDomainServiceImpl implements IPostDomainService {

    private final PostRepository postRepository;

    @Override
    public PageResult<SysPostAggregate> selectPostPage(PostQuery query, long current, long size) {
        return postRepository.selectPostPage(query, current, size);
    }

    @Override
    public List<SysPostAggregate> selectPostAll(PostQuery query) {
        return postRepository.selectPostAll(query);
    }

    @Override
    public SysPostAggregate selectPostById(Long postId) {
        return postRepository.selectPostById(postId);
    }

    @Override
    public void insertPost(SysPostAggregate post) {
        post.checkPostCodeUnique(null, postRepository.checkPostCodeUnique(post.getPostCode(), null));
        post.checkPostNameUnique(null, postRepository.checkPostNameUnique(post.getPostName(), null));
        postRepository.save(post);
        log.info("新增岗位 postCode={}", post.getPostCode());
    }

    @Override
    public void updatePost(SysPostAggregate post) {
        post.checkPostCodeUnique(post.getId(), postRepository.checkPostCodeUnique(post.getPostCode(), post.getId()));
        post.checkPostNameUnique(post.getId(), postRepository.checkPostNameUnique(post.getPostName(), post.getId()));
        postRepository.save(post);
        log.info("更新岗位 id={}", post.getId());
    }

    @Override
    public void deletePostByIds(List<Long> ids) {
        for (Long id : ids) {
            if (postRepository.countUserByPostId(id) > 0) {
                SysPostAggregate post = postRepository.selectPostById(id);
                throw new BusinessException(
                        com.silverwing.common.domain.ResultCode.BUSINESS_ERROR, "post.assigned", post.getPostName());
            }
        }
        postRepository.deleteByIds(ids);
        log.info("删除岗位 ids={}", ids);
    }

    @Override
    public List<Long> getUserPostIds(Long userId) {
        return postRepository.selectPostIdsByUserId(userId);
    }

    @Override
    public void assignPosts(Long userId, List<Long> postIds) {
        postRepository.deleteUserPosts(userId);
        postRepository.saveUserPosts(userId, postIds);
        log.info("分配用户岗位 userId={} postIds={}", userId, postIds);
    }
}
