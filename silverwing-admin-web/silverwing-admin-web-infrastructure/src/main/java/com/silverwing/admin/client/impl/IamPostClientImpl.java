package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SavePostCommand;
import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.application.query.PostPageQuery;
import com.silverwing.admin.client.IamPostClient;
import com.silverwing.admin.client.convertor.PostConvertor;
import com.silverwing.biz.post.domain.adapter.repository.PostRepository;
import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;
import com.silverwing.biz.post.domain.service.IPostDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Post 子域防腐层实现
 *
 * @author silverwing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamPostClientImpl implements IamPostClient {

    private final IPostDomainService postDomainService;
    private final PostRepository postRepository;

    @Override
    public PostResponse create(SavePostCommand command) {
        SysPostAggregate post = PostConvertor.toAggregate(command);
        postDomainService.insertPost(post);
        return PostConvertor.toResponse(post);
    }

    @Override
    public void update(Long id, SavePostCommand command) {
        SysPostAggregate post = postRepository.selectPostById(id);
        if (post == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.post.notfound");
        }
        SysPostAggregate updated = PostConvertor.toAggregate(command);
        updated.setId(id);
        postDomainService.updatePost(updated);
    }

    @Override
    public void delete(List<Long> ids) {
        postDomainService.deletePostByIds(ids);
    }

    @Override
    public PageResult<PostResponse> list(PostPageQuery query) {
        com.silverwing.biz.post.domain.model.query.PostQuery postQuery =
                new com.silverwing.biz.post.domain.model.query.PostQuery();
        postQuery.setPostCode(query.getPostCode());
        postQuery.setPostName(query.getPostName());
        postQuery.setStatus(query.getStatus());
        PageResult<SysPostAggregate> page = postDomainService.selectPostPage(
                postQuery, query.getCurrent(), query.getSize());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(PostConvertor::toResponse).collect(Collectors.toList()));
    }

    @Override
    public List<PostResponse> listAll() {
        return postDomainService.selectPostAll(null).stream()
                .map(PostConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse getById(Long id) {
        SysPostAggregate post = postRepository.selectPostById(id);
        if (post == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.post.notfound");
        }
        return PostConvertor.toResponse(post);
    }

    @Override
    public List<PostResponse> optionSelect() {
        return postDomainService.selectPostAll(null).stream()
                .filter(p -> Objects.equals("0", p.getStatus()))
                .map(PostConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserPostIds(Long userId) {
        return postDomainService.getUserPostIds(userId);
    }

    @Override
    public void assignPosts(Long userId, List<Long> postIds) {
        postDomainService.assignPosts(userId, postIds);
    }
}
