package com.silverwing.biz.post.domain.model.query;

import lombok.Data;

/**
 * 岗位查询条件
 *
 * @author silverwing
 */
@Data
public class PostQuery {

    /** 岗位编码（模糊） */
    private String postCode;

    /** 岗位名称（模糊） */
    private String postName;

    /** 状态（0 正常 1 停用） */
    private String status;
}
