package com.silverwing.biz.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户持久化对象（PO）
 * <p>
 * 与数据库表 sys_user 一一对应，仅承载数据，不包含领域行为。
 * 通过 {@code UserInfraConvertor} 与领域实体 {@link com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate} 互转。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUserPO extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private Integer sex;
    private String password;
    private String avatar;
    private String phone;
    private String email;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

}
