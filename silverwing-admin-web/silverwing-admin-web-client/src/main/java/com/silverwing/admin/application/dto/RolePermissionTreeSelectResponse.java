package com.silverwing.admin.application.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色关联权限树返回对象（对应 RuoYi 的 roleMenuTreeselect）
 */
@Data
public class RolePermissionTreeSelectResponse {

    /** 角色已勾选的权限ID集合 */
    private List<Long> checkedKeys;

    /** 完整权限树（用于前端树形展示） */
    private List<TreeSelect> menus;

    public RolePermissionTreeSelectResponse() {
    }

    public RolePermissionTreeSelectResponse(List<Long> checkedKeys, List<TreeSelect> menus) {
        this.checkedKeys = checkedKeys;
        this.menus = menus;
    }
}
