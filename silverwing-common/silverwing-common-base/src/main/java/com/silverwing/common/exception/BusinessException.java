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
 * 国际化支持：可通过 {@link #i18nCode} 指定国际化消息 code，
 * {@link GlobalExceptionHandler} 会自动翻译为当前语言的文案。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 方式1：直接抛消息（兼容旧代码，不做国际化）
 * throw new BusinessException("用户名或密码错误");
 *
 * // 方式2：使用 ResultCode 枚举
 * throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
 *
 * // 方式3：使用国际化 code（推荐，自动翻译）
 * throw new BusinessException(ResultCode.UNAUTHORIZED, "auth.login.username.or.password.error");
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
     * 国际化消息 code（可选）
     * <p>不为 null 时，GlobalExceptionHandler 会通过 MessageUtils 翻译为当前语言文案</p>
     */
    private final String i18nCode;

    /**
     * 国际化占位符参数（可选）
     */
    private final transient Object[] i18nArgs;

    // ==================== 构造方法 ====================

    /**
     * 构造业务异常 - 仅消息（默认 500 错误码，不做国际化）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
        this.i18nCode = null;
        this.i18nArgs = null;
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
        this.i18nCode = null;
        this.i18nArgs = null;
    }

    /**
     * 构造业务异常 - 基于 ResultCode 枚举（不做国际化）
     *
     * @param resultCode 状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.i18nCode = null;
        this.i18nArgs = null;
    }

    /**
     * 构造业务异常 - 基于 ResultCode 枚举，覆盖消息（不做国际化）
     *
     * @param resultCode 状态码枚举
     * @param message    自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.i18nCode = null;
        this.i18nArgs = null;
    }

    /**
     * 构造业务异常 - 带原因链（不做国际化）
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
        this.i18nCode = null;
        this.i18nArgs = null;
    }

    /**
     * 构造国际化业务异常（推荐）
     * <p>通过 i18nCode 指定国际化消息 code，由 GlobalExceptionHandler 自动翻译</p>
     *
     * @param code     错误码
     * @param i18nCode 国际化消息 code，如 auth.login.account.disabled
     * @param i18nArgs 占位符参数
     */
    public BusinessException(Integer code, String i18nCode, Object... i18nArgs) {
        super(i18nCode);
        this.code = code;
        this.message = i18nCode;
        this.i18nCode = i18nCode;
        this.i18nArgs = i18nArgs;
    }

    /**
     * 构造国际化业务异常 - 基于 ResultCode 枚举
     *
     * @param resultCode 状态码枚举
     * @param i18nCode   国际化消息 code
     * @param i18nArgs   占位符参数
     */
    public BusinessException(ResultCode resultCode, String i18nCode, Object... i18nArgs) {
        super(i18nCode);
        this.code = resultCode.getCode();
        this.message = i18nCode;
        this.i18nCode = i18nCode;
        this.i18nArgs = i18nArgs;
    }
}
