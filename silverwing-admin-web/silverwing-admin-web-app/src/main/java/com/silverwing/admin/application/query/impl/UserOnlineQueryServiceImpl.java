package com.silverwing.admin.application.query.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.SaManager;
import com.silverwing.admin.application.dto.UserOnlineVO;
import com.silverwing.admin.application.query.UserOnlineQueryService;
import com.silverwing.common.constant.RedisConstants;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.domain.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 在线用户查询服务实现（CQRS 读侧）
 * <p>通过 Sa-Token 的 Redis 令牌存储枚举在线会话，聚合为 {@link UserOnlineVO}。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
public class UserOnlineQueryServiceImpl implements UserOnlineQueryService {

    @Override
    public PageResult<UserOnlineVO> list(String ipaddr, String userName, Long current, Long size) {
        String prefix = RedisConstants.TOKEN_PREFIX;
        List<String> tokenKeys = SaManager.getSaTokenDao().searchData(prefix, "", 0, -1, false);
        List<UserOnlineVO> all = new ArrayList<>();
        for (String tokenKey : tokenKeys) {
            // token 在 Redis 中的值即为 loginId
            String loginIdStr;
            try {
                loginIdStr = SaManager.getSaTokenDao().get(tokenKey);
            } catch (Exception e) {
                continue;
            }
            if (loginIdStr == null || loginIdStr.isBlank()) {
                continue;
            }
            Long userId = parseUserId(loginIdStr);
            SaSession session = StpUtil.getSessionByLoginId(loginIdStr);
            if (session == null) {
                continue;
            }
            // token 值取 key 去除前缀后的部分，用于强退
            String tokenValue = tokenKey.substring(prefix.length());
            UserOnlineVO vo = UserOnlineVO.builder()
                    .tokenId(tokenValue)
                    .userId(userId)
                    .userName(session.getString(SaSessionConstants.USERNAME))
                    .ipaddr(session.getString(SaSessionConstants.LOGIN_IP))
                    .browser(session.getString(SaSessionConstants.LOGIN_BROWSER))
                    .os(session.getString(SaSessionConstants.LOGIN_OS))
                    .loginTime(toLocalDateTime(session.getCreateTime()))
                    .build();
            all.add(vo);
        }

        // 按条件过滤
        List<UserOnlineVO> filtered = new ArrayList<>();
        for (UserOnlineVO vo : all) {
            if (ipaddr != null && !ipaddr.isBlank()
                    && (vo.getIpaddr() == null || !vo.getIpaddr().contains(ipaddr))) {
                continue;
            }
            if (userName != null && !userName.isBlank()
                    && (vo.getUserName() == null || !vo.getUserName().contains(userName))) {
                continue;
            }
            filtered.add(vo);
        }

        // 内存分页
        long total = filtered.size();
        long cur = current == null || current < 1 ? 1 : current;
        long sz = size == null || size < 1 ? 10 : size;
        long from = Math.min((cur - 1) * sz, total);
        long to = Math.min(from + sz, total);
        List<UserOnlineVO> records = from >= to ? List.of() : filtered.subList((int) from, (int) to);
        return new PageResult<>(cur, sz, total, records);
    }

    @Override
    public void forceLogout(String tokenId) {
        StpUtil.logoutByTokenValue(tokenId);
        log.info("强制下线 token={}", tokenId);
    }

    private Long parseUserId(String loginIdStr) {
        try {
            return Long.valueOf(loginIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(long epochMilli) {
        if (epochMilli <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
