package com.silverwing.biz.dept.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门持久化对象（PO），对应 sys_dept 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept")
public class SysDeptPO extends BaseLogicEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

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
}
