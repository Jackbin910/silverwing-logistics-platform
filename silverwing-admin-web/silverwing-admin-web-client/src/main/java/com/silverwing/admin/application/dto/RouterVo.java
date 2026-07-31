package com.silverwing.admin.application.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 前端路由视图对象（对应 RuoYi 的 RouterVo）
 */
@Data
public class RouterVo {

    /** 路由名称 */
    private String name;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 重定向地址 */
    private String redirect;

    /** 是否隐藏（true 隐藏） */
    private boolean hidden;

    /** 路由参数 */
    private Map<String, Object> query;

    /** 元信息 */
    private MetaVo meta;

    /** 子路由 */
    private List<RouterVo> children;
}
