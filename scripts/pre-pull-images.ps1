# Docker 镜像预拉取脚本 - Windows PowerShell
# 用于快速拉取所有基础设施镜像，加速部署流程

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SilverWing Docker 镜像预拉取工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义需要拉取的镜像列表（与 onepanel-docker-compose.yml 保持一致）
$images = @(
    # 数据库与缓存
    @{Name="docker.1ms.run/library/mysql:8.0.40";              Desc="MySQL 数据库"},
    @{Name="docker.1ms.run/library/redis:7.0.15";              Desc="Redis 缓存"},

    # 服务治理与消息
    @{Name="docker.1ms.run/nacos/nacos-server:v2.4.3";         Desc="Nacos 服务注册中心"},
    @{Name="docker.1ms.run/library/rabbitmq:3.13.7-management"; Desc="RabbitMQ 消息队列"},

    # 任务调度与网关
    @{Name="docker.1ms.run/xuxueli/xxl-job-admin:2.4.2";       Desc="XXL-JOB 分布式任务调度"},
    @{Name="docker.1ms.run/library/nginx:1.26-alpine";         Desc="Nginx 反向代理"}
)

$totalImages = $images.Length
$successCount = 0
$failCount = 0

Write-Host "📦 计划拉取 $totalImages 个基础设施镜像..." -ForegroundColor Yellow
Write-Host ""

# 检查 Docker 是否运行
try {
    docker ps | Out-Null
} catch {
    Write-Host "❌ 错误：Docker 未运行或无法连接" -ForegroundColor Red
    Write-Host "请启动 Docker Desktop 后重试" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Docker 连接正常" -ForegroundColor Green
Write-Host ""

# 逐个拉取镜像
foreach ($imageInfo in $images) {
    $image = $imageInfo.Name
    $desc = $imageInfo.Desc

    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "⏳ 正在拉取：$desc" -ForegroundColor Yellow
    Write-Host "   镜像：$image" -ForegroundColor Gray
    Write-Host ""

    # 执行拉取
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    docker pull $image
    $stopwatch.Stop()

    $elapsedTime = "{0:N1}" -f $stopwatch.Elapsed.TotalSeconds

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 成功 [$elapsedTime 秒]" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "❌ 失败" -ForegroundColor Red
        $failCount++
    }

    Write-Host ""
}

# 统计结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  拉取完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "总镜像数：$totalImages" -ForegroundColor White
Write-Host "成功：$successCount" -ForegroundColor Green
Write-Host "失败：$failCount" -ForegroundColor Red
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "🎉 所有镜像拉取成功！可以开始部署了" -ForegroundColor Green
} else {
    Write-Host "⚠️  部分镜像拉取失败，请检查网络连接" -ForegroundColor Yellow
    Write-Host "提示：确认 docker.1ms.run 镜像加速源可达" -ForegroundColor Gray
}

Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 确认环境变量：Copy-Item onepanel.env.example onepanel.env" -ForegroundColor Cyan
Write-Host "2. 启动基础设施：docker compose -f onepanel-infra-compose.yml up -d" -ForegroundColor Cyan
Write-Host "3. 启动微服务：docker compose -f onepanel-services-compose.yml up -d" -ForegroundColor Cyan
Write-Host "（或一键部署：docker compose -f onepanel-docker-compose.yml up -d）" -ForegroundColor Cyan
Write-Host ""
