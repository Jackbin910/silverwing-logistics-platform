package com.silverwing.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询请求基类
 * <p>
 * 统一分页参数封装，Controller 接收前端分页请求时继承或直接使用。
 * 默认值：第 1 页，每页 10 条。
 * </p>
 *
 * @author silverwing
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认当前页
     */
    private static final int DEFAULT_CURRENT = 1;

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_SIZE = 10;

    /**
     * 最大每页条数（防止前端传超大值拖垮数据库）
     */
    private static final int MAX_SIZE = 500;

    /**
     * 当前页码（从 1 开始）
     */
    private Integer current = DEFAULT_CURRENT;

    /**
     * 每页条数
     */
    private Integer size = DEFAULT_SIZE;

    /**
     * 排序字段（可选）
     */
    private String sortField;

    /**
     * 排序方向：ASC / DESC
     */
    private String sortOrder;

    /**
     * 校验并规范化分页参数，防止越界值。
     * 建议在 Service 层调用本方法后再构造 MyBatis-Plus 的 Page 对象。
     */
    public void normalize() {
        if (current == null || current < 1) {
            current = DEFAULT_CURRENT;
        }
        if (size == null || size < 1) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }
}
