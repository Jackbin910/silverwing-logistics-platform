package com.silverwing.biz.dict.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据持久化对象（PO），对应 sys_dict_data 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict_data")
public class SysDictDataPO extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
}
