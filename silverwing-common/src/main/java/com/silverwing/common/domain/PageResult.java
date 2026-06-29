package com.silverwing.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装
 * <p>
 * 统一分页返回结构，配合 {@link PageRequest} 使用。
 * 可由 MyBatis-Plus 的 {@code IPage} 快速转换。
 * </p>
 *
 * @param <T> 列表元素类型
 * @author silverwing
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records == null ? Collections.emptyList() : records;
        // 总页数 = (总数 + 每页条数 - 1) / 每页条数
        this.pages = size == 0 ? 0 : (total + size - 1) / size;
    }

    /**
     * 快速构建空结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(1L, 10L, 0L, Collections.emptyList());
    }
}
