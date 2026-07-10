package com.silverwing.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 抽取所有实体共有的字段：主键、创建时间、更新时间、逻辑删除标记。
 * 配合 {@link com.silverwing.common.config.MybatisPlusAutoConfiguration} 中的
 * 自动填充处理器，子类无需再手动维护这些字段。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Data
 * &#64;EqualsAndHashCode(callSuper = true)
 * &#64;TableName("sys_user")
 * public class SysUserAggregate extends BaseEntity {
 *     private String username;
 *     // ... 其他业务字段
 * }
 * </pre>
 * </p>
 *
 * @author silverwing
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 创建者（插入时自动填充为当前登录用户）
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新者（插入与更新时自动填充为当前登录用户）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 创建时间（插入时自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（插入和更新时自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
