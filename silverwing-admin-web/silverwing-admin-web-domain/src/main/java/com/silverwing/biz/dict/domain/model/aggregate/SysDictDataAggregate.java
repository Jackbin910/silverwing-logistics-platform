package com.silverwing.biz.dict.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据聚合根，对应数据库表 sys_dict_data。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictDataAggregate extends DomainEntity {

    /** 字典数据主键 */
    private Long id;

    /** 字典排序 */
    private Long dictSort;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 字典类型 */
    private String dictType;

    /** 样式属性（其他样式扩展） */
    private String cssClass;

    /** 表格字典样式 */
    private String listClass;

    /** 是否默认（Y-是 N-否） */
    private String isDefault;

    /** 状态（0-正常 1-停用） */
    private String status;

    /** 备注 */
    private String remark;
}
