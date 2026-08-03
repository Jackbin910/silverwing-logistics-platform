package com.silverwing.biz.post.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.post.domain.adapter.repository.PostRepository;
import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;
import com.silverwing.biz.post.domain.model.query.PostQuery;
import com.silverwing.biz.post.infrastructure.adapter.repository.convertor.PostInfraConvertor;
import com.silverwing.biz.post.infrastructure.dao.SysPostDao;
import com.silverwing.biz.post.infrastructure.dao.SysUserPostDao;
import com.silverwing.biz.post.infrastructure.dao.po.SysPostPO;
import com.silverwing.biz.post.infrastructure.dao.po.SysUserPostPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位仓储实现（防腐层）
 *
 * @author silverwing
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final SysPostDao sysPostDao;

    private final SysUserPostDao sysUserPostDao;

    private LambdaQueryWrapper<SysPostPO> buildWrapper(PostQuery query) {
        LambdaQueryWrapper<SysPostPO> wrapper = Wrappers.lambdaQuery();
        if (query != null) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getPostCode())) {
                wrapper.like(SysPostPO::getPostCode, query.getPostCode());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getPostName())) {
                wrapper.like(SysPostPO::getPostName, query.getPostName());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getStatus())) {
                wrapper.eq(SysPostPO::getStatus, query.getStatus());
            }
        }
        wrapper.orderByAsc(SysPostPO::getPostSort);
        return wrapper;
    }

    @Override
    public PageResult<SysPostAggregate> selectPostPage(PostQuery query, long current, long size) {
        IPage<SysPostPO> page = new Page<>(current, size);
        List<SysPostPO> records = sysPostDao.selectPostList(buildWrapper(query));
        page.setRecords(records);
        page.setTotal(records.size());
        List<SysPostAggregate> list = records.stream()
                .map(PostInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }

    @Override
    public List<SysPostAggregate> selectPostAll(PostQuery query) {
        List<SysPostPO> records = sysPostDao.selectPostAll(buildWrapper(query));
        return records.stream()
                .map(PostInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public SysPostAggregate selectPostById(Long postId) {
        SysPostPO po = sysPostDao.selectById(postId);
        return po == null ? null : PostInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public boolean checkPostCodeUnique(String postCode, Long excludeId) {
        LambdaQueryWrapper<SysPostPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysPostPO::getPostCode, postCode);
        if (excludeId != null) {
            wrapper.ne(SysPostPO::getId, excludeId);
        }
        return sysPostDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkPostNameUnique(String postName, Long excludeId) {
        LambdaQueryWrapper<SysPostPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysPostPO::getPostName, postName);
        if (excludeId != null) {
            wrapper.ne(SysPostPO::getId, excludeId);
        }
        return sysPostDao.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SysPostAggregate post) {
        SysPostPO po = PostInfraConvertor.INSTANCE.toPo(post);
        if (post.getId() == null) {
            sysPostDao.insert(po);
            post.setId(po.getId());
        } else {
            sysPostDao.updateById(po);
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        sysPostDao.deleteBatchIds(ids);
        for (Long id : ids) {
            LambdaQueryWrapper<SysUserPostPO> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(SysUserPostPO::getPostId, id);
            sysUserPostDao.delete(wrapper);
        }
    }

    @Override
    public Long countUserByPostId(Long postId) {
        LambdaQueryWrapper<SysUserPostPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserPostPO::getPostId, postId);
        return sysUserPostDao.selectCount(wrapper);
    }

    @Override
    public List<Long> selectPostIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserPostPO> wrapper = Wrappers.lambdaQuery();
        wrapper.select(SysUserPostPO::getPostId).eq(SysUserPostPO::getUserId, userId);
        return sysUserPostDao.selectObjs(wrapper).stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toList());
    }

    @Override
    public void saveUserPosts(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        for (Long postId : postIds) {
            SysUserPostPO po = new SysUserPostPO();
            po.setUserId(userId);
            po.setPostId(postId);
            sysUserPostDao.insert(po);
        }
    }

    @Override
    public void deleteUserPosts(Long userId) {
        LambdaQueryWrapper<SysUserPostPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserPostPO::getUserId, userId);
        sysUserPostDao.delete(wrapper);
    }
}
