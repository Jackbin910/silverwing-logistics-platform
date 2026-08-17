package com.silverwing.common.aspect;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.i18n.LocaleContextUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 操作日志切面抽象基类
 * <p>
 * 统一封装 {@link Log} 注解拦截、请求上下文采集、字段截断与异步落库逻辑，
 * 避免各业务模块（admin-web / ai-service 等）重复实现导致逻辑漂移。
 * </p>
 * <p>
 * 各业务模块继承本类并提供自身的 {@code SysOperLogPO} 类型与 {@link #doInsert} 实现即可；
 * 如需采集操作人员所属部门（如 IAM 模块），覆写 {@link #fillOperator} 追加部门反查。
 * </p>
 * <p>
 * 落库经 DynamicTP 线程池 {@code operLogExecutor} 异步执行，不阻塞请求线程；
 * 记录过程异常隔离，任何失败都不影响主业务流程。
 * </p>
 *
 * @param <T> 各业务模块的操作日志 PO 类型
 * @author silverwing
 */
@Slf4j
@Aspect
@Order
public abstract class AbstractOperLogAspect<T> {

    /** 字段最大长度，与 sys_oper_log 表中 varchar(2000) 对齐 */
    protected static final int MAX_LENGTH = 2000;

    /** DynamicTP 在 Spring 容器注册的同名 Bean，须与 application.yml 中 threadPoolName 一致 */
    @Resource
    private ThreadPoolExecutor operLogExecutor;

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
        T po = createPo();
        setOperTime(po, LocalDateTime.now());
        setStatus(po, 0);

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Log logAnno = signature.getMethod().getAnnotation(Log.class);
            setTitle(po, logAnno.title());
            setBusinessType(po, logAnno.businessType().getCode());
            setOperatorType(po, logAnno.operatorType());
            setMethod(po, signature.getDeclaringTypeName() + "." + signature.getName());

            fillRequestInfo(po, joinPoint.getArgs(), logAnno.saveResult());

            Object result = joinPoint.proceed();
            if (logAnno.saveResult()) {
                setJsonResult(po, truncate(JSON.toJSONString(result)));
            }
            return result;
        } catch (Throwable throwable) {
            setStatus(po, 1);
            setErrorMsg(po, truncate(throwable.getMessage()));
            throw throwable;
        } finally {
            setCostTime(po, System.currentTimeMillis() - startTime);
            record(po);
        }
    }

    /**
     * 异步落库：提交一个任务到 DynamicTP 线程池，内部执行批量插入。
     * 全程异常隔离，绝不阻塞请求线程。
     *
     * @param po 操作日志 PO
     */
    private void record(T po) {
        // 包装任务以传播请求线程的 Locale，保证异步线程内的国际化取值正确
        CompletableFuture.runAsync(LocaleContextUtils.wrap(() -> {
            try {
                doInsert(Collections.singletonList(po));
            } catch (Exception e) {
                log.error("操作日志异步落库失败", e);
            }
        }), operLogExecutor);
    }

    /**
     * 采集 HTTP 请求上下文（URL / IP / 请求方式 / 操作人员）与入参
     *
     * @param po         操作日志 PO
     * @param args       方法入参
     * @param saveResult 是否记录入参
     */
    private void fillRequestInfo(T po, Object[] args, boolean saveResult) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            setRequestMethod(po, request.getMethod());
            setOperUrl(po, request.getRequestURI());
            setOperIp(po, JakartaServletUtil.getClientIP(request));
            if (saveResult) {
                setOperParam(po, truncate(JSON.toJSONString(args)));
            }
            fillOperator(po);
        } catch (Exception e) {
            // 非 Web 上下文或序列化失败时忽略，保证主流程不受影响
            log.debug("采集操作日志请求上下文失败", e);
        }
    }

    /**
     * 从 Sa-Token 获取当前登录用户作为操作人员（默认实现，不带部门反查）。
     * <p>子类可覆写以追加部门反查等扩展信息。</p>
     *
     * @param po 操作日志 PO
     */
    protected void fillOperator(T po) {
        try {
            // 优先取登录时写入会话的用户名作为操作人
            SaSession session = StpUtil.getSession();
            String username = session.getString(SaSessionConstants.USERNAME);
            if (username != null && !username.isBlank()) {
                setOperName(po, username);
                return;
            }
            // 兜底取 Sa-Token 登录标识（通常为 userId）
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                setOperName(po, String.valueOf(loginId));
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
    protected String truncate(String str) {
        if (str == null) {
            return "";
        }
        return str.length() > MAX_LENGTH ? str.substring(0, MAX_LENGTH) : str;
    }

    // ===== 以下为子类需要实现的抽象方法与 PO 字段 setter 钩子 =====

    /**
     * 创建对应模块的操作日志 PO 实例
     *
     * @return 操作日志 PO
     */
    protected abstract T createPo();

    /**
     * 持久化操作日志（由子类调用自身 Mapper 的 insertBatch 实现）
     *
     * @param list 操作日志 PO 列表
     */
    protected abstract void doInsert(List<T> list);

    /** 设置模块标题 */
    protected abstract void setTitle(T po, String title);

    /** 设置业务类型编码 */
    protected abstract void setBusinessType(T po, Integer code);

    /** 设置操作类别 */
    protected abstract void setOperatorType(T po, Integer operatorType);

    /** 设置目标方法全限定名 */
    protected abstract void setMethod(T po, String method);

    /** 设置请求方式 */
    protected abstract void setRequestMethod(T po, String requestMethod);

    /** 设置操作URL */
    protected abstract void setOperUrl(T po, String operUrl);

    /** 设置操作IP */
    protected abstract void setOperIp(T po, String operIp);

    /** 设置入参 */
    protected abstract void setOperParam(T po, String operParam);

    /** 设置返回结果 */
    protected abstract void setJsonResult(T po, String jsonResult);

    /** 设置操作状态（0成功 1失败） */
    protected abstract void setStatus(T po, Integer status);

    /** 设置异常信息 */
    protected abstract void setErrorMsg(T po, String errorMsg);

    /** 设置操作人 */
    protected abstract void setOperName(T po, String operName);

    /** 设置操作时间 */
    protected abstract void setOperTime(T po, LocalDateTime operTime);

    /** 设置耗时（毫秒） */
    protected abstract void setCostTime(T po, Long costTime);
}
