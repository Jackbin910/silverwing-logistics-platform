package com.silverwing.admin.application.command.impl;

import com.silverwing.admin.application.command.LogininforCommandService;
import com.silverwing.admin.client.LogininforClient;
import com.silverwing.common.constant.RedisConstants;
import com.silverwing.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统访问记录命令服务实现。
 *
 * @author silverwing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogininforCommandServiceImpl implements LogininforCommandService {

    private final LogininforClient logininforClient;

    private final RedisUtil redisUtil;

    @Override
    public void removeByIds(Long[] infoIds) {
        logininforClient.removeByIds(infoIds);
    }

    @Override
    public void clean() {
        logininforClient.clean();
    }

    @Override
    public void unlock(String userName) {
        String key = RedisConstants.PWD_ERR_CNT_PREFIX + userName;
        redisUtil.delete(key);
        log.info("管理员解锁账号 userName={}", userName);
    }
}
