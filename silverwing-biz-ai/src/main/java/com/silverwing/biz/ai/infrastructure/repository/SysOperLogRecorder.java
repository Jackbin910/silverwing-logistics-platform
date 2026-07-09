package com.silverwing.biz.ai.infrastructure.repository;

import com.silverwing.biz.ai.infrastructure.dao.po.SysOperLogPO;
import com.silverwing.biz.ai.infrastructure.mapper.SysOperLogMapper;
import com.silverwing.common.operlog.OperLog;
import com.silverwing.common.operlog.OperLogRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI 操作日志记录器
 * <p>
 * 将 common 采集的 {@link OperLog} 转换为 {@link SysOperLogPO} 并写入 silverwing_ai 库，
 * 供 ai-service 复用统一的操作日志采集框架。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SysOperLogRecorder implements OperLogRecorder {

    private final SysOperLogMapper sysOperLogMapper;

    @Override
    public void record(OperLog operLog) {
        SysOperLogPO po = new SysOperLogPO();
        po.setTitle(operLog.getTitle());
        po.setBusinessType(operLog.getBusinessType());
        po.setMethod(operLog.getMethod());
        po.setRequestMethod(operLog.getRequestMethod());
        po.setOperatorType(operLog.getOperatorType());
        po.setOperName(operLog.getOperName());
        po.setDeptName(operLog.getDeptName());
        po.setOperUrl(operLog.getOperUrl());
        po.setOperIp(operLog.getOperIp());
        po.setOperLocation(operLog.getOperLocation());
        po.setOperParam(operLog.getOperParam());
        po.setJsonResult(operLog.getJsonResult());
        po.setStatus(operLog.getStatus());
        po.setErrorMsg(operLog.getErrorMsg());
        po.setOperTime(operLog.getOperTime());
        po.setCostTime(operLog.getCostTime());
        sysOperLogMapper.insert(po);
    }
}
