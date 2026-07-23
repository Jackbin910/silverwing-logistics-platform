-- 轻量 WAF（OpenResty 接入层）
-- 仅检查请求特征（URI / 查询参数 / 请求头），不读取请求体、不触碰鉴权。
-- 命中攻击特征直接返回 403，减轻后端压力。

local _M = {}

-- 攻击特征（大小写不敏感匹配）
local PATTERNS = {
    '%.%.%/',                       -- 路径穿越（unix）
    '%.%.%\\',                       -- 路径穿越（windows）
    '<script',                       -- XSS
    'javascript:',
    'onerror%s*=',
    'onload%s*=',
    'onclick%s*=',
    'eval%s*%(',
    'select%s+[%w%s_(),.]*%s+from',  -- SQL 注入
    'union%s+select',
    'insert%s+into',
    'drop%s+table',
    'delete%s+from',
    'update%s+%w+%s+set',
    '1%s*=%s*1',
    "'%s+or%s+'",
    '/etc/passwd',
    'cmd%.exe',
    'powershell',
    'wget%s+http',
    'curl%s+http',
    'base64_decode',
    'phpinfo',
}

--- 判断字符串是否包含攻击特征
local function contains_attack(s)
    if not s or s == '' then
        return false
    end
    s = string.lower(s)
    for _, p in ipairs(PATTERNS) do
        if string.find(s, p) then
            return true
        end
    end
    return false
end

--- 返回 403 拦截响应
local function reject()
    ngx.status = 403
    ngx.header['Content-Type'] = 'application/json; charset=utf-8'
    ngx.say('{"code":403,"msg":"请求被 WAF 拦截"}')
    return ngx.exit(403)
end

--- 接入层 WAF 检查
function _M.check()
    -- 1. 完整请求 URI（含路径与查询串，不受 rewrite 影响）
    if contains_attack(ngx.var.request_uri) then
        return reject()
    end

    -- 2. 查询参数
    if contains_attack(ngx.var.args) then
        return reject()
    end

    -- 3. 常见请求头
    local headers = ngx.req.get_headers()
    for _, v in pairs(headers) do
        if type(v) == 'string' and contains_attack(v) then
            return reject()
        end
    end
end

return _M
