package com.silverwing.admin.application.dto;

import lombok.Data;

/**
 * 路由元信息（前端侧边栏展示用）
 */
@Data
public class MetaVo {

    /** 菜单名称 */
    private String title;

    /** 菜单图标 */
    private String icon;

    /** 是否不缓存（true 不缓存） */
    private boolean noCache;

    /** 外链地址（内链打开方式） */
    private String link;
}
