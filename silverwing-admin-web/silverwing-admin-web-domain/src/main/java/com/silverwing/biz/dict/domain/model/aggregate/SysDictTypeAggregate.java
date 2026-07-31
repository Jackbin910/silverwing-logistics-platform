package com.silverwing.biz.dict.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型聚合根，对应数据库表 sys_dict_type。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictTypeAggregate extends DomainEntity {

    /** 字典主键 */
    private Long id;

    /** 字典名称 */
    private String dictName;

    /** 字典类型（唯一标识，如 sys_user_sex） */
    private String dictType;

    /** 状态（0-正常 1-停用） */
    private String status;

    /** 备注 */
    private String remark;
}
