package com.silverwing.biz.ops.infrastructure.adapter.h800.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * H800 转换服务统一返回结构
 * 成功判定约定为 code == 1（与 warehouse_automatic 保持一致）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class H800CommonResult<T> {

    /**
     * 状态码，1 表示成功
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 是否成功（约定成功码为 1）
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return code != null && code.equals(1);
    }
}
