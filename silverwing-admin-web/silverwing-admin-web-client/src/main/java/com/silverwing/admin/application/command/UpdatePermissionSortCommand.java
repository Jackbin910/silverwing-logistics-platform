package com.silverwing.admin.application.command;

import lombok.Data;

import java.util.List;

/**
 * 权限（菜单）排序保存命令
 */
@Data
public class UpdatePermissionSortCommand {

    /** 按展示顺序排列的权限ID列表 */
    private List<Long> ids;
}
