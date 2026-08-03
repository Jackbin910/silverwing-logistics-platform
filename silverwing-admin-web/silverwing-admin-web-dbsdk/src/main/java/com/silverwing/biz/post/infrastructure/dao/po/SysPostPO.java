package com.silverwing.biz.post.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import com.silverwing.common.entity.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位持久化对象（对应 sys_post 表）
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPostPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 显示顺序
     */
    private Integer postSort;

    /**
     * 状态（0 正常 1 停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户关联计数（非表字段，列表/导出时填充）
     */
    @TableField(exist = false)
    private Integer userCount;

    /**
     * 登录用户岗位ID集合（非表字段）
     */
    @TableField(exist = false)
    private Long postId;

    /**
     * 登录用户ID（非表字段，列表/导出时用于计数）
     */
    @TableField(exist = false)
    private Long userId;

}

