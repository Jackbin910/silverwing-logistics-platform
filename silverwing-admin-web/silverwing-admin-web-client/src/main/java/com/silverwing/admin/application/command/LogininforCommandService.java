package com.silverwing.admin.application.command;

/**
 * 系统访问记录命令服务端口。
 * <p>定义访问记录的删除与清空能力，由基础设施层实现。</p>
 *
 * @author silverwing
 */
public interface LogininforCommandService {

    /**
     * 批量删除访问记录。
     *
     * @param infoIds 访问ID数组
     */
    void removeByIds(Long[] infoIds);

    /**
     * 清空全部访问记录。
     */
    void clean();

    /**
     * 解锁账号：清除指定用户的密码错误计数缓存，使其可立即重新登录。
     *
     * @param userName 用户账号
     */
    void unlock(String userName);
}
