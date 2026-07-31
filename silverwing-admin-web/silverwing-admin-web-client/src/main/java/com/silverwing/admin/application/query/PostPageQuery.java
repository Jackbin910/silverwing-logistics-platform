package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;

/**
 * 岗位分页查询对象
 *
 * @author silverwing
 */
@Data
public class PostPageQuery extends PageRequest  {

    /** 岗位编码（模糊） */
    private String postCode;

    /** 岗位名称（模糊） */
    private String postName;

    /** 状态（0 正常 1 停用） */
    private String status;
}
