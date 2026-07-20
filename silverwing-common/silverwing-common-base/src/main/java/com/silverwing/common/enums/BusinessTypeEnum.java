package com.silverwing.common.enums;

import lombok.Getter;

/**
 * 业务操作类型枚举
 * <p>对应 {@link com.silverwing.common.annotation.Log#businessType()}，
 * 用于操作日志 sys_oper_log 表的 business_type 字段。</p>
 */
@Getter
public enum BusinessTypeEnum {

    /**
     * 其它
     */
    OTHER(0, "其它"),

    /**
     * 新增
     */
    INSERT(1, "新增"),

    /**
     * 修改
     */
    UPDATE(2, "修改"),

    /**
     * 删除
     */
    DELETE(3, "删除");

    /**
     * 类型编码
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    BusinessTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据类型编码获取枚举
     *
     * @param code 类型编码
     * @return 匹配的枚举，未匹配时返回 null
     */
    public static BusinessTypeEnum getByCode(Integer code) {
        for (BusinessTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
