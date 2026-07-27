package com.silverwing.biz.ops.infrastructure.adapter.h800.dto;

import lombok.Data;

/**
 * H800 接驳仓开门请求入参
 * 与 H800 转换服务 /api/base/h800/open 接口契约保持一致
 */
@Data
public class H800OpenWareInputDTO {

    /**
     * 库位编号，例如 12-A-01
     */
    private String location;

    /**
     * 请求唯一标识，用于幂等追踪与日志串联
     */
    private String requestId;

    /**
     * 出口编号（开门场景暂不使用，置空保留以兼容后续接口）
     */
    private Integer exitNum;
}
