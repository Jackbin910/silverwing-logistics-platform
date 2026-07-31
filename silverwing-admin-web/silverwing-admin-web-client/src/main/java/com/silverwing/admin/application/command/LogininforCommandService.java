package com.silverwing.admin.application.command;

import com.silverwing.admin.client.LogininforClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录日志命令应用服务。
 */
@Service
@RequiredArgsConstructor
public class LogininforCommandService {

    private final LogininforClient logininforClient;

    /** 新增登录日志（内部调用，记录登录行为） */
    public void add(SaveLogininforCommand command) {
        logininforClient.add(command);
    }

    /** 批量删除登录日志 */
    public void removeByIds(List<Long> infoIds) {
        logininforClient.removeByIds(infoIds);
    }

    /** 清空登录日志 */
    public void clean() {
        logininforClient.clean();
    }

    /** 解锁用户账户（清除密码错误次数缓存） */
    public void unlock(String userName) {
        logininforClient.unlock(userName);
    }
}
