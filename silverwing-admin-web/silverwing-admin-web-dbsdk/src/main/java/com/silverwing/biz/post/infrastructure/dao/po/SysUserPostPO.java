package com.silverwing.biz.post.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户与岗位关联持久化对象（对应 sys_user_post 表）
 *
 * @author silverwing
 */
@Data
@TableName("sys_user_post")
public class SysUserPostPO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 岗位ID */
    private Long postId;
}
