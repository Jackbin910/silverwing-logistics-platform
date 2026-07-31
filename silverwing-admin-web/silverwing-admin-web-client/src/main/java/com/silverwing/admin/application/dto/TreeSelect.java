package com.silverwing.admin.application.dto;

import lombok.Data;

import java.util.List;

/**
 * 树形下拉节点（权限/菜单树选择使用）
 */
@Data
public class TreeSelect {

    /** 节点ID */
    private Long id;

    /** 显示标签 */
    private String label;

    /** 父级ID */
    private Long parentId;

    /** 资源类型（menu/button/api） */
    private String resourceType;

    /** 权限标识 */
    private String perms;

    /** 子节点 */
    private List<TreeSelect> children;

    public TreeSelect() {
    }

    public TreeSelect(Long id, String label, Long parentId, String resourceType, String perms) {
        this.id = id;
        this.label = label;
        this.parentId = parentId;
        this.resourceType = resourceType;
        this.perms = perms;
    }
}
