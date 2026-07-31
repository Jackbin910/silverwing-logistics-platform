package com.silverwing.common.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 逻辑删除实体基类（继承自 BaseEntity）。
 * <p>
 * 在父类审计字段基础上增加 deleted 逻辑删除标记，适用于带 deleted 列的表。
 * </p>
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseLogicEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 逻辑删除标记（0-未删除，1-已删除） */
    @TableLogic
    private Integer deleted;
}
