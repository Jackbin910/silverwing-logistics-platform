package com.silverwing.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.i18n.MessageUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截所有 Controller 层抛出的异常，返回标准 {@link Result} 格式。
 * 所有异常均会记录日志并包含 traceId，便于问题排查。
 * </p>
 * <p>
 * 通过 {@link ConditionalOnWebApplication} 限定仅在 Servlet Web 环境生效，
 * Gateway（WebFlux）不依赖 common，不会触发本类。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 业务异常处理
     * <p>优先使用 i18nCode 翻译为当前语言文案，降级使用原 message</p>
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        String displayMessage = resolveMessage(e);
        log.warn("业务异常 [code={}]：{}", e.getCode(), displayMessage);
        return Result.fail(e.getCode(), displayMessage);
    }

    /**
     * 解析国际化消息
     * <p>如果异常携带了 i18nCode，则通过 MessageUtils 翻译；否则使用原始 message</p>
     *
     * @param e 业务异常
     * @return 当前语言文案
     */
    private String resolveMessage(BusinessException e) {
        if (e.getI18nCode() != null) {
            return MessageUtils.get(e.getI18nCode(), (Object[]) e.getI18nArgs());
        }
        return e.getMessage();
    }

    // ==================== 认证授权异常（Sa-Token） ====================

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.warn("认证异常：{}", e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足：{}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    // ==================== 参数校验异常 ====================

    /**
     * 请求体参数校验异常（@Valid @RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常：{}", message);
        return Result.fail(ResultCode.PARAM_VALIDATE_ERROR, message);
    }

    /**
     * 表单绑定参数校验异常（@Valid 表单提交）
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常：{}", message);
        return Result.fail(ResultCode.PARAM_VALIDATE_ERROR, message);
    }

    /**
     * 单参数校验异常（@Validated 方法参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验异常：{}", message);
        return Result.fail(ResultCode.PARAM_VALIDATE_ERROR, message);
    }

    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParamException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.fail(ResultCode.BAD_REQUEST, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 请求体解析异常（JSON 格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析异常：{}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求数据格式错误");
    }

    // ==================== HTTP 请求异常 ====================

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMethod());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED, "请求方法不支持: " + e.getMethod());
    }

    /**
     * 404 未找到
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在：{}", e.getRequestURL());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    // ==================== 兜底异常 ====================

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数：{}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 系统异常兜底
     * <p>
     * 捕获所有未处理的异常，隐藏内部细节，防止敏感信息泄露
     * </p>
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletResponse response) {
        if (response.getContentType() != null && response.getContentType().contains("text/event-stream")) {
            log.error("SSE 流处理异常: {}", e.getMessage());
            return null;
        }
        Throwable rootCause = getRootCause(e);
        // 栈溢出特殊处理：常见于非法 BCrypt 哈希或递归调用，需要单独提示
        if (e instanceof ServletException && rootCause instanceof StackOverflowError) {
            log.error("系统异常：请求处理发生栈溢出，可能存在递归调用或非法 BCrypt 哈希", rootCause);
            return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
        }

        log.error("系统异常：{}", rootCause.getMessage(), rootCause);
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 获取异常根因，避免只记录 Handler dispatch failed 这类包装异常。
     *
     * @param throwable 当前异常
     * @return 异常根因
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
