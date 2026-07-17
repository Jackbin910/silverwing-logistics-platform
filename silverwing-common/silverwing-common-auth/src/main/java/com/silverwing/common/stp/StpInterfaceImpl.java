package com.silverwing.common.stp;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.common.constant.SaSessionConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源实现
 * <p>
 * 该实现从 Sa-Token Session（基于 Redis 共享）中读取用户的角色与权限列表，
 * 避免各微服务直接查询权限库（业务服务与权限库 silverwing_logistics 物理隔离）。
 * </p>
 * <p>
 * 数据写入时机：用户登录成功后，由 auth 服务将角色、权限写入 Session。
 * 权限变更时，auth 服务通过 {@link StpUtil#getSessionByLoginId(Object)} 刷新会话。
 * </p>
 */
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取当前用户的权限标识列表
     * 用于 @SaCheckPermission 注解校验
     *
     * @param loginId   登录ID
     * @param loginType 账号类型
     * @return 权限标识列表，如 ["system:user:list", "logistics:order:create"]
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            List<String> list = session.get(SaSessionConstants.PERMISSION_LIST, new ArrayList<>());
            return list;
        } catch (Exception e) {
            // 读取失败时返回空列表，拒绝所有权限，保证安全
            log.warn("获取用户权限列表失败 loginId={}：{}", loginId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取当前用户的角色编码列表
     * 用于 @SaCheckRole 注解校验
     *
     * @param loginId   登录ID
     * @param loginType 账号类型
     * @return 角色编码列表，如 ["ADMIN", "USER"]
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            List<String> list = session.get(SaSessionConstants.ROLE_LIST, new ArrayList<>());
            return list;
        } catch (Exception e) {
            log.warn("获取用户角色列表失败 loginId={}：{}", loginId, e.getMessage());
            return new ArrayList<>();
        }
    }

}
