package com.silverwing.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.operlog.OperLog;
import com.silverwing.common.operlog.OperLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.dev33.satoken.stp.StpUtil;
import java.util.List;

/**
 * 操作日志切面
 * <p>
 * 拦截所有被 {@link Log} 标记的方法，采集耗时、入参、返回与异常，
 * 并委派给容器中注册的 {@link OperLogRecorder} 列表落库。
 * 记录过程全程异常隔离，任何失败都不影响主业务流程。
 * </p>
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class OperLogAspect {

    /** 字段最大长度，与 sys_oper_log 表中 varchar(2000) 对齐 */
    private static final int MAX_LENGTH = 2000;

    private final List<OperLogRecorder> recorders;

    /**
     * 环绕 @Log 注解方法，采集并落库操作日志
     */
    @Around("@annotation(com.silverwing.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperLog operLog = new OperLog();
        operLog.setOperTime(java.time.LocalDateTime.now());
        operLog.setStatus(0);

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Log logAnno = signature.getMethod().getAnnotation(Log.class);
            operLog.setTitle(logAnno.title());
            operLog.setBusinessType(logAnno.businessType());
            operLog.setOperatorType(logAnno.operatorType());
            operLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            fillRequestInfo(operLog, joinPoint.getArgs(), logAnno.saveResult());

            Object result = joinPoint.proceed();
            if (logAnno.saveResult()) {
                operLog.setJsonResult(truncate(JSON.toJSONString(result)));
            }
            return result;
        } catch (Throwable throwable) {
            operLog.setStatus(1);
            operLog.setErrorMsg(truncate(throwable.getMessage()));
            throw throwable;
        } finally {
            operLog.setCostTime(System.currentTimeMillis() - startTime);
            record(operLog);
        }
    }

    /**
     * 采集 HTTP 请求上下文（URL / IP / 请求方式 / 操作人员）与入参
     */
    private void fillRequestInfo(OperLog operLog, Object[] args, boolean saveResult) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperUrl(request.getRequestURI());
            operLog.setOperIp(getClientIp(request));
            if (saveResult) {
                operLog.setOperParam(truncate(JSON.toJSONString(args)));
            }
            fillOperator(operLog);
        } catch (Exception e) {
            // 非 Web 上下文或序列化失败时忽略，保证主流程不受影响
            log.debug("采集操作日志请求上下文失败", e);
        }
    }

    /**
     * 从 Sa-Token 获取当前登录用户作为操作人员
     */
    private void fillOperator(OperLog operLog) {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                operLog.setOperName(String.valueOf(loginId));
            }
        } catch (Exception e) {
            // 未登录或令牌不可用，操作人员留空
        }
    }

    /**
     * 优先取 X-Forwarded-For / X-Real-IP，兜底取远程地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? "" : ip;
    }

    private boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 委派给所有已注册的记录器，单个失败不影响其他记录器与主流程
     */
    private void record(OperLog operLog) {
        if (recorders == null || recorders.isEmpty()) {
            return;
        }
        for (OperLogRecorder recorder : recorders) {
            try {
                recorder.record(operLog);
            } catch (Exception e) {
                log.error("操作日志落库失败", e);
            }
        }
    }

    /**
     * 超出字段长度时截断，避免入库失败
     */
    private String truncate(String str) {
        if (str == null) {
            return "";
        }
        return str.length() > MAX_LENGTH ? str.substring(0, MAX_LENGTH) : str;
    }
}
