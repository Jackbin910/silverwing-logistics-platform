package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.command.UpdatePermissionSortCommand;
import com.silverwing.admin.application.dto.MetaVo;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.dto.RolePermissionTreeSelectResponse;
import com.silverwing.admin.application.dto.RouterVo;
import com.silverwing.admin.application.dto.TreeSelect;
import com.silverwing.admin.application.query.PermissionPageQuery;
import com.silverwing.admin.client.IamPermissionClient;
import com.silverwing.admin.client.convertor.PermissionConvertor;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.PermissionQuery;
import com.silverwing.biz.iam.domain.service.IPermissionDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IAM 权限上下文防腐层适配器
 * <p>本类是唯一直接依赖 biz-iam 权限领域层（聚合根、仓储、领域服务）的地方。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamPermissionClientImpl implements IamPermissionClient {

    private final PermissionRepository permissionRepository;
    private final PermissionConvertor permissionConvertor;
    private final IPermissionDomainService permissionDomainService;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public PermissionResponse create(SavePermissionCommand command) {
        // 同级名称唯一性校验
        permissionDomainService.checkPermissionNameUnique(null, command.getParentId(), command.getPermissionName());
        // 路由名称唯一性校验
        permissionDomainService.checkRouteConfigUnique(null, command.getRouteName());
        SysPermissionAggregate permission = new SysPermissionAggregate();
        permissionConvertor.applyCommandToEntity(permission, command);
        permission.enable();
        // 领域服务负责权限持久化
        permission = permissionDomainService.save(permission);
        log.info("新建权限成功 permissionCode={}, id={}",
                command.getPermissionCode(), permission.getId());
        return permissionConvertor.toResponse(permission);
    }

    @Override
    @Transactional
    public void update(Long id, SavePermissionCommand command) {
        SysPermissionAggregate permission = permissionRepository.findById(id);
        if (permission == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.permission.notfound");
        }
        // 同级名称唯一性校验
        permissionDomainService.checkPermissionNameUnique(id, command.getParentId(), command.getPermissionName());
        // 路由名称唯一性校验
        permissionDomainService.checkRouteConfigUnique(id, command.getRouteName());
        permissionConvertor.applyCommandToEntity(permission, command);
        // 领域服务负责权限持久化
        permissionDomainService.save(permission);
        log.info("更新权限 id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除（含子级与角色占用校验）
        permissionDomainService.deleteById(id);
        log.info("删除权限 id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream()
                .map(permissionConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PermissionResponse> page(PermissionPageQuery query) {
        PermissionQuery permissionQuery = toPermissionQuery(query);
        PageResult<SysPermissionAggregate> page = permissionRepository.findPage(permissionQuery);
        List<PermissionResponse> records = page.getRecords().stream()
                .map(permissionConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(Long id) {
        SysPermissionAggregate permission = permissionRepository.findById(id);
        return permission == null ? null : permissionConvertor.toResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeSelect> treeSelect(PermissionPageQuery query) {
        PermissionQuery permissionQuery = toPermissionQuery(query);
        List<SysPermissionAggregate> list = permissionRepository.findList(permissionQuery);
        return buildTreeSelect(list);
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionTreeSelectResponse rolePermissionTreeSelect(Long roleId) {
        List<SysPermissionAggregate> all = permissionRepository.findAll();
        List<TreeSelect> menus = buildTreeSelect(all);
        List<Long> checkedKeys = permissionRepository.findIdsByRoleId(roleId);
        return new RolePermissionTreeSelectResponse(checkedKeys, menus);
    }

    @Override
    @Transactional
    public void updateSort(UpdatePermissionSortCommand command) {
        permissionDomainService.updateSort(command.getIds());
        log.info("保存权限排序 ids={}", command.getIds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouterVo> getRouters(Long userId) {
        List<SysRoleAggregate> roles = roleRepository.findRolesByUserId(userId);
        boolean isAdmin = roles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role.getRoleCode()));
        List<SysPermissionAggregate> all = permissionRepository.findAll();
        List<SysPermissionAggregate> menus;
        if (isAdmin) {
            // 管理员加载全部菜单
            menus = all.stream().filter(SysPermissionAggregate::isMenu).collect(Collectors.toList());
        } else {
            // 普通用户仅加载已分配权限（含祖先节点）的菜单
            Set<Long> assignedIds = roles.stream()
                    .flatMap(role -> permissionRepository.findIdsByRoleId(role.getId()).stream())
                    .collect(Collectors.toSet());
            Map<Long, SysPermissionAggregate> map = all.stream()
                    .collect(Collectors.toMap(SysPermissionAggregate::getId, m -> m));
            Set<Long> fullIds = new HashSet<>(assignedIds);
            for (Long id : new HashSet<>(assignedIds)) {
                SysPermissionAggregate node = map.get(id);
                if (node == null) {
                    continue;
                }
                Long pid = node.getParentId();
                while (pid != null && pid != 0L && map.containsKey(pid) && !fullIds.contains(pid)) {
                    fullIds.add(pid);
                    pid = map.get(pid).getParentId();
                }
            }
            menus = all.stream()
                    .filter(m -> fullIds.contains(m.getId()) && m.isMenu())
                    .collect(Collectors.toList());
        }
        return buildRouters(menus);
    }

    /**
     * 将扁平权限列表构建为树形下拉结构
     */
    private List<TreeSelect> buildTreeSelect(List<SysPermissionAggregate> list) {
        Map<Long, TreeSelect> nodeMap = new LinkedHashMap<>();
        List<TreeSelect> roots = new ArrayList<>();
        for (SysPermissionAggregate p : list) {
            TreeSelect node = new TreeSelect(p.getId(), p.getPermissionName(),
                    p.getParentId(), p.getResourceType(), p.getPerms());
            nodeMap.put(p.getId(), node);
        }
        for (TreeSelect node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(node);
            } else {
                TreeSelect parent = nodeMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    /**
     * 将菜单列表构建为前端路由树
     */
    private List<RouterVo> buildRouters(List<SysPermissionAggregate> menus) {
        Map<Long, List<SysPermissionAggregate>> childrenMap = new LinkedHashMap<>();
        for (SysPermissionAggregate m : menus) {
            Long pid = m.getParentId() == null ? 0L : m.getParentId();
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(m);
        }
        List<SysPermissionAggregate> roots = childrenMap.getOrDefault(0L, new ArrayList<>());
        return roots.stream().map(root -> toRouter(root, childrenMap)).collect(Collectors.toList());
    }

    private RouterVo toRouter(SysPermissionAggregate menu, Map<Long, List<SysPermissionAggregate>> childrenMap) {
        RouterVo router = new RouterVo();
        router.setHidden(menu.getVisible() != null && menu.getVisible() == 1);
        // 路由名称：优先使用 routeName，否则取 path
        String name = menu.getRouteName();
        if (!StringUtils.hasText(name)) {
            name = menu.getPath();
        }
        router.setName(StringUtils.capitalize(name));
        router.setPath(getRouterPath(menu));
        router.setComponent(getComponent(menu));
        MetaVo meta = new MetaVo();
        meta.setTitle(menu.getPermissionName());
        meta.setIcon(menu.getIcon());
        // 是否缓存：isCache=0 缓存（noCache=false），=1 不缓存（noCache=true）
        meta.setNoCache(menu.getIsCache() == null || menu.getIsCache() == 1);
        // 外链时设置 link
        if (menu.getIsFrame() != null && menu.getIsFrame() == 0) {
            meta.setLink(menu.getPath());
        }
        router.setMeta(meta);
        if (StringUtils.hasText(menu.getQuery())) {
            router.setQuery(parseQuery(menu.getQuery()));
        }
        List<SysPermissionAggregate> children = childrenMap.getOrDefault(menu.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            List<RouterVo> childRouters = children.stream()
                    .map(c -> toRouter(c, childrenMap))
                    .collect(Collectors.toList());
            router.setChildren(childRouters);
            // 多个子节点时设置 noRedirect，避免自动跳转首个子菜单
            if (childRouters.size() > 1) {
                router.setRedirect("noRedirect");
            }
        }
        return router;
    }

    /**
     * 计算路由地址（对应 RuoYi getRouterPath）
     */
    private String getRouterPath(SysPermissionAggregate menu) {
        String path = menu.getPath() == null ? "" : menu.getPath();
        boolean topLevel = menu.getParentId() == null || menu.getParentId() == 0L;
        boolean innerLink = menu.getIsFrame() != null && menu.getIsFrame() == 0;
        if (topLevel) {
            if (innerLink) {
                // 外链：http(s) 直接作为地址，否则补前导斜杠
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    return path;
                }
                return "/" + path;
            }
            // 顶级目录
            return "/" + StringUtils.uncapitalize(path);
        } else {
            if (innerLink && (path.startsWith("http://") || path.startsWith("https://"))) {
                return path;
            }
            return path;
        }
    }

    /**
     * 计算组件路径（对应 RuoYi getComponent）
     */
    private String getComponent(SysPermissionAggregate menu) {
        boolean topLevel = menu.getParentId() == null || menu.getParentId() == 0L;
        boolean innerLink = menu.getIsFrame() != null && menu.getIsFrame() == 0;
        if (innerLink) {
            return "InnerLink";
        }
        if (topLevel) {
            return "Layout";
        }
        return menu.getComponent();
    }

    /**
     * 解析路由参数字符串（k=v&k2=v2）为 Map
     */
    private Map<String, Object> parseQuery(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                result.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return result;
    }

    /**
     * 将本模块权限分页查询条件翻译为 biz-iam 领域查询对象
     */
    private PermissionQuery toPermissionQuery(PermissionPageQuery query) {
        PermissionQuery permissionQuery = new PermissionQuery();
        permissionQuery.setCurrent(query.getCurrent());
        permissionQuery.setSize(query.getSize());
        permissionQuery.setKeyword(query.getKeyword());
        permissionQuery.setStatus(query.getStatus());
        return permissionQuery;
    }
}
