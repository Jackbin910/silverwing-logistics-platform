package com.silverwing.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类（父类）。
 * <p>
 * 仅抽取所有实体共有的审计字段，不含逻辑删除标记。需要逻辑删除（deleted 列）的表，
 * 请继承 {@link BaseLogicEntity}。
 * </p>
 * <p>
 * 配合 MybatisPlusAutoConfiguration 中的自动填充处理器，子类无需手动维护审计字段。
 * </p>
 *
 * @author silverwing
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建者（插入时自动填充为当前登录用户） */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 更新者（插入与更新时自动填充为当前登录用户） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

