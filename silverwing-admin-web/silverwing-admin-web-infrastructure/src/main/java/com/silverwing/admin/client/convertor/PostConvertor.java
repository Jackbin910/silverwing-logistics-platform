package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.command.SavePostCommand;
import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;

/**
 * 岗位应用层转换器（ACL 防腐层）
 *
 * @author silverwing
 */
public final class PostConvertor {

    private PostConvertor() {
    }

    /**
     * 领域实体 -> 应用层响应
     */
    public static PostResponse toResponse(SysPostAggregate post) {
        if (post == null) {
            return null;
        }
        PostResponse response = new PostResponse();
        response.setPostId(post.getId());
        response.setPostCode(post.getPostCode());
        response.setPostName(post.getPostName());
        response.setPostSort(post.getPostSort());
        response.setStatus(post.getStatus());
        response.setRemark(post.getRemark());
        response.setUserCount(post.getUserCount());
        response.setCreateTime(post.getCreateTime());
        response.setUpdateTime(post.getUpdateTime());
        return response;
    }

    /**
     * 创建/更新命令 -> 领域实体
     */
    public static SysPostAggregate toAggregate(SavePostCommand command) {
        SysPostAggregate post = new SysPostAggregate();
        post.setPostCode(command.getPostCode());
        post.setPostName(command.getPostName());
        post.setPostSort(command.getPostSort());
        post.setStatus(command.getStatus());
        post.setRemark(command.getRemark());
        return post;
    }
}
