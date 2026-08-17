package com.silverwing.biz.ai.infrastructure.aspect;

import com.silverwing.biz.ai.infrastructure.dao.po.SysOperLogPO;
import com.silverwing.biz.ai.infrastructure.mapper.SysOperLogMapper;
import com.silverwing.common.aspect.AbstractOperLogAspect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 操作日志切面
 * <p>
 * 继承 {@link AbstractOperLogAspect} 复用拦截、采集、截断与异步落库能力，
 * 落库目标为 silverwing_ai 库的 sys_oper_log 表。本模块无需部门反查，直接使用基类默认实现。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogAspect extends AbstractOperLogAspect<SysOperLogPO> {

    private final SysOperLogMapper sysOperLogMapper;

    @Override
    protected SysOperLogPO createPo() {
        return new SysOperLogPO();
    }

    @Override
    protected void doInsert(List<SysOperLogPO> list) {
        sysOperLogMapper.insertBatch(list);
    }

    @Override
    protected void setTitle(SysOperLogPO po, String title) {
        po.setTitle(title);
    }

    @Override
    protected void setBusinessType(SysOperLogPO po, Integer code) {
        po.setBusinessType(code);
    }

    @Override
    protected void setOperatorType(SysOperLogPO po, Integer operatorType) {
        po.setOperatorType(operatorType);
    }

    @Override
    protected void setMethod(SysOperLogPO po, String method) {
        po.setMethod(method);
    }

    @Override
    protected void setRequestMethod(SysOperLogPO po, String requestMethod) {
        po.setRequestMethod(requestMethod);
    }

    @Override
    protected void setOperUrl(SysOperLogPO po, String operUrl) {
        po.setOperUrl(operUrl);
    }

    @Override
    protected void setOperIp(SysOperLogPO po, String operIp) {
        po.setOperIp(operIp);
    }

    @Override
    protected void setOperParam(SysOperLogPO po, String operParam) {
        po.setOperParam(operParam);
    }

    @Override
    protected void setJsonResult(SysOperLogPO po, String jsonResult) {
        po.setJsonResult(jsonResult);
    }

    @Override
    protected void setStatus(SysOperLogPO po, Integer status) {
        po.setStatus(status);
    }

    @Override
    protected void setErrorMsg(SysOperLogPO po, String errorMsg) {
        po.setErrorMsg(errorMsg);
    }

    @Override
    protected void setOperName(SysOperLogPO po, String operName) {
        po.setOperName(operName);
    }

    @Override
    protected void setOperTime(SysOperLogPO po, LocalDateTime operTime) {
        po.setOperTime(operTime);
    }

    @Override
    protected void setCostTime(SysOperLogPO po, Long costTime) {
        po.setCostTime(costTime);
    }
}
