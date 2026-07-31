package com.silverwing.biz.dict.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型持久化对象（PO），对应 sys_dict_type 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict_type")
public class SysDictTypePO extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典名称 */
    private String dictName;

    /** 字典类型 */
    private String dictType;

    /** 状态（0-正常 1-停用） */
    private String status;
}
