package com.silverwing.common.domain.model;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageRequest {

    private String username;
    private Integer status;
}
