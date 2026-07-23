-- 接入层限流（OpenResty）
-- 基于共享内存字典的固定窗口计数，零第三方依赖。
-- 仅做流量防护，不读取 token、不触碰鉴权逻辑（鉴权仍在 Gateway 层）。

local _M = {}

-- 共享字典在 nginx.conf 中通过 lua_shared_dict 声明
local dict = ngx.shared.limit_req_store

-- 全局限制：单 IP 每秒最多 30 次请求
local GLOBAL_LIMIT = 30
-- AI 接口限制：单 IP 对 /ai/** 每秒最多 10 次（Ollama 资源敏感）
local AI_LIMIT = 10

--- 返回 429 限流响应
local function reject()
    ngx.status = 429
    ngx.header['Content-Type'] = 'application/json; charset=utf-8'
    ngx.header['Retry-After'] = '1'
    ngx.say('{"code":429,"msg":"请求过于频繁，请稍后重试"}')
    return ngx.exit(429)
end

--- 接入层限流检查
function _M.check()
    if not dict then
        ngx.log(ngx.ERR, 'limit_req_store 共享字典未配置')
        return
    end

    local ip = ngx.var.binary_remote_addr or 'unknown'
    local uri = ngx.var.request_uri or ''

    -- AI 接口走更严格的限制
    local is_ai = uri:find('/ai/', 1, true) or uri:find('/api/ai/', 1, true)
    local limit = is_ai and AI_LIMIT or GLOBAL_LIMIT
    local key = 'rl:' .. (is_ai and 'ai:' or 'g:') .. ip

    -- 固定窗口：key 不存在则初始化为 0，1 秒过期
    dict:add(key, 0, 1)
    local count, err = dict:incr(key, 1)
    if not count then
        -- 并发下 add 与 incr 之间存在极小窗口，兜底写入一次
        count = dict:add(key, 1, 1) and 1 or 0
    end

    if count and count > limit then
        return reject()
    end
end

return _M
