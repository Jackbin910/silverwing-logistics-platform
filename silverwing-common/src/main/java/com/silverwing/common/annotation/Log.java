package com.silverwing.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标记在 Controller（HTTP 入口）方法上，由各业务模块的 {@code OperLogAspect} 自动采集：
 * 方法耗时、入参、返回结果、异常信息以及请求上下文（URL / IP / 请求方式 / 操作人员），
 * 并直接组装为本模块的 sys_oper_log PO 异步批量落库到本服务数据库。
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块标题
     */
    String title() default "";

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    int businessType() default 0;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    int operatorType() default 0;

    /**
     * 是否记录返回参数（返回体较大时可关闭，避免超出字段长度）
     */
    boolean saveResult() default true;
}
