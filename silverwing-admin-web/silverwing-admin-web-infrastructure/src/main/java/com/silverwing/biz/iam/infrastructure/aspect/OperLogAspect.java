package com.silverwing.biz.iam.infrastructure.aspect;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.biz.dept.infrastructure.dao.SysDeptDao;
import com.silverwing.biz.dept.infrastructure.dao.po.SysDeptPO;
import com.silverwing.biz.iam.infrastructure.dao.SysOperLogMapper;
import com.silverwing.biz.iam.infrastructure.dao.po.SysOperLogPO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserPO;
import com.silverwing.biz.iam.infrastructure.dao.SysUserDao;
import com.silverwing.common.aspect.AbstractOperLogAspect;
import com.silverwing.common.constant.SaSessionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IAM 操作日志切面
 * <p>
 * 继承 {@link AbstractOperLogAspect} 复用拦截、采集、截断与异步落库能力，
 * 仅补充 IAM 特有的「按登录用户反查部门名称」逻辑，使操作日志可按部门检索。
 * 落库目标为 silverwing_logistics 库的 sys_oper_log 表。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogAspect extends AbstractOperLogAspect<SysOperLogPO> {

    private final SysOperLogMapper sysOperLogMapper;
    private final SysUserDao sysUserDao;
    private final SysDeptDao sysDeptDao;

    @Override
    protected SysOperLogPO createPo() {
        return new SysOperLogPO();
    }

    @Override
    protected void doInsert(List<SysOperLogPO> list) {
        sysOperLogMapper.insertBatch(list);
    }

    @Override
    protected void fillOperator(SysOperLogPO po) {
        try {
            // 先取会话中的用户名（与基类一致），再追加部门反查
            super.fillOperator(po);
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                SysUserPO user = sysUserDao.selectById(Long.valueOf(String.valueOf(loginId)));
                if (user != null && user.getDeptId() != null) {
                    SysDeptPO dept = sysDeptDao.selectById(user.getDeptId());
                    if (dept != null) {
                        po.setDeptName(dept.getDeptName());
                    }
                }
            }
        } catch (Exception e) {
            // 未登录或令牌不可用，部门留空
            log.warn("采集操作日志部门失败", e);
        }
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
