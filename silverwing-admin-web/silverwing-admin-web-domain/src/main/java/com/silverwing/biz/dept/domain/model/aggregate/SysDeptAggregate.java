package com.silverwing.biz.dept.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门聚合根，对应数据库表 sys_dept。
 * <p>
 * 承载部门核心业务属性，包含祖级列表（ancestors）以便快速构建部门树。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptAggregate extends DomainEntity {

    /** 部门ID */
    private Long deptId;

    /** 父部门ID */
    private Long parentId;

    /** 祖级列表（逗号分隔的祖先部门ID） */
    private String ancestors;

    /** 部门名称 */
    private String deptName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 部门状态（0-正常 1-停用） */
    private String status;

    /** 父部门名称（仅用于展示，不持久化） */
    private String parentName;
}
