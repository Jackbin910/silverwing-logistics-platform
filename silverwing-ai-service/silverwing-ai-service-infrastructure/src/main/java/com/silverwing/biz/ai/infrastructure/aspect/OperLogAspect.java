package com.silverwing.biz.ai.infrastructure.aspect;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.silverwing.biz.ai.infrastructure.dao.po.SysOperLogPO;
import com.silverwing.biz.ai.infrastructure.mapper.SysOperLogMapper;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.i18n.LocaleContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.dynamictp.core.DtpRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AI 操作日志切面
 * <p>
 * 拦截被 {@link Log} 标记的方法，采集耗时、入参、返回与异常及请求上下文，
 * 直接组装为 {@link SysOperLogPO} 并通过 {@link SysOperLogMapper#insertBatch} 落库到 silverwing_ai 库。
 * 落库经 DynamicTP 线程池 {@code operLogExecutor} 异步执行，不阻塞请求线程；
 * 记录过程异常隔离，任何失败都不影响主业务流程。
 * </p>
 */
@Slf4j
@Aspect
@Component
@Order
@RequiredArgsConstructor
public class OperLogAspect {

    /** 字段最大长度，与 sys_oper_log 表中 varchar(2000) 对齐 */
    private static final int MAX_LENGTH = 2000;

    /** DynamicTP 线程池名称，须与 application.yml 中 spring.dynamic.tp.executors[].threadPoolName 一致 */
    private static final String DTP_EXECUTOR_NAME = "operLogExecutor";

    private final SysOperLogMapper sysOperLogMapper;

    /**
     * 环绕 @Log 注解方法，采集并异步落库操作日志
     *
     * @param joinPoint 连接点
     * @return 目标方法返回值
     * @throws Throwable 透传目标方法异常
     */
    @Around("@annotation(com.silverwing.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperLogPO po = new SysOperLogPO();
        po.setOperTime(LocalDateTime.now());
        po.setStatus(0);

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Log logAnno = signature.getMethod().getAnnotation(Log.class);
            po.setTitle(logAnno.title());
            po.setBusinessType(logAnno.businessType().getCode());
            po.setOperatorType(logAnno.operatorType());
            po.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            fillRequestInfo(po, joinPoint.getArgs(), logAnno.saveResult());

            Object result = joinPoint.proceed();
            if (logAnno.saveResult()) {
                po.setJsonResult(truncate(JSON.toJSONString(result)));
            }
            return result;
        } catch (Throwable throwable) {
            po.setStatus(1);
            po.setErrorMsg(truncate(throwable.getMessage()));
            throw throwable;
        } finally {
            po.setCostTime(System.currentTimeMillis() - startTime);
            record(po);
        }
    }

    /**
     * 异步落库：提交一个任务到 DynamicTP 线程池，内部执行批量插入。
     * 全程异常隔离，绝不阻塞请求线程。
     *
     * @param po 操作日志 PO
     */
    private void record(SysOperLogPO po) {
        Executor executor = DtpRegistry.getExecutor(DTP_EXECUTOR_NAME);
        // 包装任务以传播请求线程的 Locale，保证异步线程内的国际化取值正确
        CompletableFuture.runAsync(LocaleContextUtils.wrap(() -> {
            try {
                sysOperLogMapper.insertBatch(Collections.singletonList(po));
            } catch (Exception e) {
                log.error("操作日志异步落库失败", e);
            }
        }), executor);
    }

    /**
     * 采集 HTTP 请求上下文（URL / IP / 请求方式 / 操作人员）与入参
     *
     * @param po         操作日志 PO
     * @param args       方法入参
     * @param saveResult 是否记录入参
     */
    private void fillRequestInfo(SysOperLogPO po, Object[] args, boolean saveResult) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            po.setRequestMethod(request.getMethod());
            po.setOperUrl(request.getRequestURI());
            po.setOperIp(JakartaServletUtil.getClientIP(request));
            if (saveResult) {
                po.setOperParam(truncate(JSON.toJSONString(args)));
            }
            fillOperator(po);
        } catch (Exception e) {
            // 非 Web 上下文或序列化失败时忽略，保证主流程不受影响
            log.debug("采集操作日志请求上下文失败", e);
        }
    }

    /**
     * 从 Sa-Token 获取当前登录用户作为操作人员
     *
     * @param po 操作日志 PO
     */
    private void fillOperator(SysOperLogPO po) {
        try {
            // 优先取登录时写入会话的用户名作为操作人
            SaSession session = StpUtil.getSession();
            String username = session.getString(SaSessionConstants.USERNAME);
            if (username != null && !username.isBlank()) {
                po.setOperName(username);
                return;
            }
            // 兜底取 Sa-Token 登录标识（通常为 userId）
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                po.setOperName(String.valueOf(loginId));
            }
        } catch (Exception e) {
            // 未登录或令牌不可用，操作人员留空
        }
    }

    /**
     * 超出字段长度时截断，避免入库失败
     *
     * @param str 原始字符串
     * @return 截断后的字符串
     */
    private String truncate(String str) {
        if (str == null) {
            return "";
        }
        return str.length() > MAX_LENGTH ? str.substring(0, MAX_LENGTH) : str;
    }
}
