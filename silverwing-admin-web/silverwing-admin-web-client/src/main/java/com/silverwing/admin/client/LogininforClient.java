package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveLogininforCommand;
import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 登录日志上下文防腐层接口。
 */
public interface LogininforClient {

    /** 分页查询登录日志 */
    PageResult<LogininforResponse> list(LogininforPageQuery query);

    /** 导出查询（不分页） */
    List<LogininforResponse> listExport(LogininforPageQuery query);

    /** 根据ID查询 */
    LogininforResponse getById(Long infoId);

    /** 新增登录日志（内部调用，记录登录行为） */
    void add(SaveLogininforCommand command);

    /** 批量删除登录日志 */
    void removeByIds(List<Long> infoIds);

    /** 清空登录日志 */
    void clean();

    /** 解锁用户账户（清除密码错误次数缓存） */
    void unlock(String userName);
}
