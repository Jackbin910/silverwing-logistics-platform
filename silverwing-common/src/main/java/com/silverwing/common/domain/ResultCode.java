package com.silverwing.common.domain;

import lombok.Getter;

/**
 * 统一状态码枚举
 * <p>
 * 集中管理所有业务返回状态码，消除散落在 Result/BusinessException/GlobalExceptionHandler
 * 中的魔法数字。新增状态码时统一在此处添加。
 * </p>
 *
 * @author silverwing
 */
@Getter
public enum ResultCode {

    // ==================== 通用 ====================
    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    /** 操作失败 */
    FAIL(500, "操作失败"),

    // ==================== 客户端错误 4xx ====================
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录 / 登录已过期 */
    UNAUTHORIZED(401, "登录已过期，请重新登录"),
    /** 无操作权限 */
    FORBIDDEN(403, "无操作权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // ==================== 服务端错误 5xx ====================
    /** 系统繁忙 */
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后重试"),
    /** 服务不可用 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // ==================== 业务错误 1xxx ====================
    /** 业务异常通用码 */
    BUSINESS_ERROR(1000, "业务处理失败"),
    /** 参数校验失败 */
    PARAM_VALIDATE_ERROR(1001, "参数校验失败"),
    /** 数据不存在 */
    DATA_NOT_FOUND(1002, "数据不存在"),
    /** 数据已存在 */
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    /** 数据状态非法 */
    DATA_STATUS_ILLEGAL(1004, "数据状态非法");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 提示消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
