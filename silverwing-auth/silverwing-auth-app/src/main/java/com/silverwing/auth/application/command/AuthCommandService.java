package com.silverwing.auth.application.command;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.silverwing.auth.application.dto.LoginResponse;
import com.silverwing.auth.application.service.CaptchaService;
import com.silverwing.auth.application.service.PasswordRetryService;
import com.silverwing.auth.config.RsaKeyConfig;
import com.silverwing.auth.iam.domain.adapter.repository.LogininforRepository;
import com.silverwing.auth.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.auth.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.auth.iam.domain.adapter.repository.UserRepository;
import com.silverwing.auth.iam.domain.model.aggregate.LogininforAggregate;
import com.silverwing.auth.iam.domain.constant.IamConstants;
import com.silverwing.auth.iam.domain.model.aggregate.AuthRoleAggregate;
import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.i18n.LocaleContextUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 认证命令服务（CQRS 写侧）
 * <p>
 * 负责登录、登出、刷新权限缓存等会改变状态的用例。
 * 仅通过 auth 自有 IAM 的 Repository 端口访问领域数据，不直接依赖 Mapper。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandService {

    @Resource
    private ThreadPoolExecutor operLogExecutor;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RsaKeyConfig rsaKeyConfig;
    private final CaptchaService captchaService;
    private final LogininforRepository logininforRepository;
    private final PasswordRetryService passwordRetryService;

    /**
     * 用户登录
     * 流程：RSA 解密密码 → 查询用户 → 状态校验 → 密码比对 → Sa-Token 签发 → 写入角色/权限到 Session
     */
    public LoginResponse login(LoginCommand command) {
        String username = command.getUsername();
        String ipaddr = command.getIpaddr();

        // 0. RSA 解密前端传入的加密密码，得到明文
        String rawPassword = decryptPassword(command.getPassword());

        // 0.5 验证码校验（开启时）：校验失败直接拦截，避免无谓的用户查询与密码解密
        if (!captchaService.validate(command.getUuid(), command.getCode())) {
            log.warn("登录失败：验证码错误 username={}", username);
            recordLogininfor(username, ipaddr, false, "验证码错误");
            throw BusinessException.i18n(ResultCode.UNAUTHORIZED, "auth.captcha.error");
        }

        // 1. 查询用户
        AuthUserAggregate user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("登录失败：用户不存在 username={}", username);
            recordLogininfor(username, ipaddr, false, "用户不存在/密码错误");
            throw BusinessException.i18n(ResultCode.UNAUTHORIZED,
                    "auth.login.username.or.password.error");
        }

        // 2. 状态校验（领域行为）
        if (!user.isActive()) {
            log.warn("登录失败：用户已禁用 username={}", username);
            recordLogininfor(username, ipaddr, false, "账号已禁用");
            throw BusinessException.i18n(ResultCode.FORBIDDEN,
                    "auth.login.account.disabled");
        }

        // 3. 密码校验与重试锁定控制（失败计数、锁定、写登录日志由 PasswordRetryService 统一处理）
        passwordRetryService.validate(username, ipaddr, matchesPassword(rawPassword, user));

        // 4. 查询角色和权限
        List<String> roleCodes = roleRepository.findRolesByUserId(user.getId()).stream()
                .map(AuthRoleAggregate::getRoleCode)
                .collect(Collectors.toList());
        // 超级管理员直接授予全部权限通配符，与 RuoYi 方案一致（无需逐条绑定权限）
        boolean isAdmin = roleCodes.stream()
                .anyMatch(code -> IamConstants.SUPER_ADMIN.equalsIgnoreCase(code));
        List<String> permissions = isAdmin
                ? List.of(IamConstants.ALL_PERMISSION)
                : permissionRepository.findPermissionCodesByUserId(user.getId());

        // 5. Sa-Token 登录，写入 Session
        StpUtil.login(user.getId());
        SaSession session = StpUtil.getSession();
        session.set(SaSessionConstants.ROLE_LIST, roleCodes);
        session.set(SaSessionConstants.PERMISSION_LIST, permissions);
        session.set(SaSessionConstants.USERNAME, user.getUsername());
        session.set(SaSessionConstants.LOGIN_IP, command.getIpaddr());
        session.set(SaSessionConstants.LOGIN_BROWSER, command.getBrowser());
        session.set(SaSessionConstants.LOGIN_OS, command.getOs());

        log.info("登录成功：username={}, userId={}, roles={}, 权限数={}",
                user.getUsername(), user.getId(), roleCodes, permissions.size());

        recordLogininfor(username, ipaddr, true, "登录成功");

        return LoginResponse.builder()
                .token(StpUtil.getTokenValue())
                .username(user.getUsername())
                .roles(roleCodes)
                .build();
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 刷新指定用户的权限缓存（Sa-Token Session）
     * 在 admin-web 修改了角色/权限后调用。
     */
    public void refreshPermissionCache(Long userId) {
        try {
            List<String> roleCodes = roleRepository.findRolesByUserId(userId).stream()
                    .map(AuthRoleAggregate::getRoleCode)
                    .collect(Collectors.toList());
            // 超级管理员授予全部权限通配符，与普通登录逻辑保持一致
            boolean isAdmin = roleCodes.stream()
                    .anyMatch(code -> IamConstants.SUPER_ADMIN.equalsIgnoreCase(code));
            List<String> permissionCodes = isAdmin
                    ? List.of(IamConstants.ALL_PERMISSION)
                    : permissionRepository.findPermissionCodesByUserId(userId);

            SaSession session = StpUtil.getSessionByLoginId(userId);
            session.set(SaSessionConstants.PERMISSION_LIST, permissionCodes);
            session.set(SaSessionConstants.ROLE_LIST, roleCodes);

            log.info("刷新用户权限缓存成功 userId={}, 权限数={}, 角色数={}",
                    userId, permissionCodes.size(), roleCodes.size());
        } catch (Exception e) {
            log.warn("刷新用户权限缓存失败 userId={}：{}", userId, e.getMessage());
        }
    }

    /**
     * 记录登录日志到 {@code sys_logininfor} 表。
     * <p>无论登录成功或失败均写入一条记录，便于审计与安全分析；写入异常仅记录日志，不影响主流程。</p>
     * <p>落库经 DynamicTP 线程池 {@code operLogExecutor} 异步执行，不阻塞登录主流程；
     * 若线程池未在配置中心声明，则降级到默认线程池，保证不抛 NPE。</p>
     *
     * @param username 用户账号
     * @param ipaddr   登录IP
     * @param success  是否成功（true=成功，false=失败）
     * @param msg      提示信息
     */
    private void recordLogininfor(String username, String ipaddr, boolean success, String msg) {
        try {
            LogininforAggregate aggregate = new LogininforAggregate();
            aggregate.setUserName(username);
            aggregate.setIpaddr(ipaddr);
            aggregate.setStatus(success ? 0 : 1);
            aggregate.setMsg(msg);
            aggregate.setAccessTime(LocalDateTime.now());

            Runnable task = LocaleContextUtils.wrap(() -> {
                try {
                    logininforRepository.insert(aggregate);
                } catch (Exception e) {
                    log.warn("写入登录日志失败 username={}, ip={}, 原因={}", username, ipaddr, e.getMessage());
                }
            });
            CompletableFuture.runAsync(task, operLogExecutor);
        } catch (Exception e) {
            log.warn("提交登录日志失败 username={}, ip={}, 原因={}", username, ipaddr, e.getMessage());
        }
    }

    /**
     * 密码安全校验（BCrypt）
     */
    private boolean matchesPassword(String rawPassword, AuthUserAggregate user) {
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || encodedPassword.isBlank()) {
            log.error("登录失败：用户密码哈希为空 userId={}, username={}",
                    user.getId(), user.getUsername());
            throw BusinessException.i18n(ResultCode.INTERNAL_SERVER_ERROR,
                    "auth.login.account.config.error");
        }
        return user.matchesPassword(rawPassword);
    }

    /**
     * RSA 解密前端传入的加密密码
     * <p>前端使用公钥加密明文密码，后端使用私钥解密还原明文</p>
     *
     * @param encryptedPassword RSA 加密后的密码（Base64）
     * @return 明文密码
     */
    private String decryptPassword(String encryptedPassword) {
        try {
            return rsaKeyConfig.getRsa().decryptStr(encryptedPassword, KeyType.PrivateKey);
        } catch (Exception e) {
            log.warn("登录失败：密码解密异常，可能未使用 RSA 加密或密钥不匹配");
            throw BusinessException.i18n(ResultCode.BAD_REQUEST,
                    "auth.login.password.decrypt.error");
        }
    }
}
