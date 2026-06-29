package com.silverwing.common.exception;

import com.silverwing.common.domain.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 业务逻辑中遇到预期内的错误时抛出，由 {@link GlobalExceptionHandler} 统一捕获
 * 转换为标准 {@link com.silverwing.common.domain.Result} 返回。
 * 推荐使用 {@link ResultCode} 枚举构造，保证错误码集中管理。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
 * throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR);
 * </pre>
 * </p>
 *
 * @author silverwing
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码（默认 500）
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造业务异常 - 仅消息（默认 500 错误码）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造业务异常 - 指定错误码和消息
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造业务异常 - 基于 ResultCode 枚举（推荐）
     *
     * @param resultCode 状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造业务异常 - 基于 ResultCode 枚举，覆盖消息
     *
     * @param resultCode 状态码枚举
     * @param message    自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 构造业务异常 - 带原因链
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }
}
