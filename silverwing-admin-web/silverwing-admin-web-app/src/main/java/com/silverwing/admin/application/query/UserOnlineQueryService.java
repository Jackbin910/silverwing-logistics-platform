package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.UserOnlineVO;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 在线用户查询服务（CQRS 读侧）
 * <p>从 Sa-Token 的登录会话中聚合在线用户，支持按 IP / 用户名过滤并分页。</p>
 *
 * @author silverwing
 */
public interface UserOnlineQueryService {

    /**
     * 查询在线用户列表（按 IP / 用户名过滤，内存分页）
     *
     * @param ipaddr   登录IP（模糊匹配，可空）
     * @param userName 用户名（模糊匹配，可空）
     * @param current  当前页
     * @param size     每页条数
     * @return 在线用户分页结果
     */
    PageResult<UserOnlineVO> list(String ipaddr, String userName, Long current, Long size);

    /**
     * 根据 token 强制踢下线
     *
     * @param tokenId 会话 token
     */
    void forceLogout(String tokenId);
}
