package com.silverwing.common.util;

import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;

/**
 * 业务断言工具类
 * <p>
 * 简化业务层参数校验与前置条件判断，避免到处写 if-throw 样板代码。
 * 断言失败时抛出 {@link BusinessException}，由全局异常处理器统一捕获。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * AssertUtil.notNull(user, ResultCode.DATA_NOT_FOUND, "用户不存在");
 * AssertUtil.notBlank(username, ResultCode.BAD_REQUEST, "用户名不能为空");
 * AssertUtil.isTrue(order.getStatus() == ORDER_STATUS_PENDING,
 *         ResultCode.DATA_STATUS_ILLEGAL, "订单状态不允许此操作");
 * </pre>
 * </p>
 *
 * @author silverwing
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    /**
     * 断言对象不为 null
     */
    public static void notNull(Object obj, ResultCode resultCode, String message) {
        if (obj == null) {
            throw new BusinessException(resultCode, message);
        }
    }

    /**
     * 断言对象不为 null（使用默认消息）
     */
    public static void notNull(Object obj, ResultCode resultCode) {
        if (obj == null) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言字符串非空白
     */
    public static void notBlank(String text, ResultCode resultCode, String message) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(resultCode, message);
        }
    }

    /**
     * 断言集合/数组非空
     */
    public static void notEmpty(java.util.Collection<?> collection, ResultCode resultCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(resultCode, message);
        }
    }

    /**
     * 断言条件为 true
     */
    public static void isTrue(boolean expression, ResultCode resultCode, String message) {
        if (!expression) {
            throw new BusinessException(resultCode, message);
        }
    }

    /**
     * 断言条件为 true（使用默认消息）
     */
    public static void isTrue(boolean expression, ResultCode resultCode) {
        if (!expression) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言条件为 false
     */
    public static void isFalse(boolean expression, ResultCode resultCode, String message) {
        if (expression) {
            throw new BusinessException(resultCode, message);
        }
    }
}
