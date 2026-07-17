package com.silverwing.common.domain;

import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一返回结果
 * <p>
 * 所有 Controller 接口统一返回本对象，确保前后端契约一致。
 * 状态码建议使用 {@link ResultCode} 枚举，避免魔法数字。
 * </p>
 *
 * @param <T> 业务数据类型
 * @author silverwing
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求链路 ID（从 MDC 中获取，方便排查）
     */
    private String traceId;

    public Result() {
        this.timestamp = System.currentTimeMillis();
        this.traceId = MDC.get("traceId");
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // ==================== 成功 ====================

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功返回 - 带消息
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功返回 - 带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回 - 带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败 ====================

    /**
     * 失败返回（默认系统错误）
     */
    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(),
                ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 失败返回 - 带消息
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    /**
     * 失败返回 - 带状态码和消息
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败返回 - 基于 ResultCode 枚举
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败返回 - 基于 ResultCode 枚举，覆盖消息
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message);
    }

    // ==================== 判断 ====================

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code.equals(ResultCode.SUCCESS.getCode());
    }
}
