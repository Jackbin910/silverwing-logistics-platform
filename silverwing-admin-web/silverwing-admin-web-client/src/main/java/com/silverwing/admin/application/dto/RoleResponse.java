package com.silverwing.admin.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色响应DTO
 * <p>由 SysRoleAggregate 经 RoleConvertor 映射得到，作为触发层对外返回的角色视图，不暴露领域聚合根。</p>
 */
@Data
public class RoleResponse implements Serializable {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer status;

    /** 数据范围（1：全部 2：自定 3：本部门 4：本部门及以下） */
    private Integer dataScope;

    /** 显示顺序 */
    private Integer roleSort;

    /** 菜单树选择项是否关联显示（1是 0否） */
    private Integer menuCheckStrictly;

    /** 部门树选择项是否关联显示（1是 0否） */
    private Integer deptCheckStrictly;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
