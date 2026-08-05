package com.silverwing.admin.application.command;

import com.silverwing.admin.client.OperLogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志命令应用服务。
 */
@Service
@RequiredArgsConstructor
public class OperLogCommandService {

    private final OperLogClient operLogClient;

    /** 批量删除操作日志 */
    public void removeByIds(List<Long> operIds) {
        operLogClient.removeByIds(operIds);
    }

    /** 清空操作日志 */
    public void clean() {
        operLogClient.clean();
    }
}
