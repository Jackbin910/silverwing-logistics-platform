$ErrorActionPreference = 'Stop'
$src = "d:\workspace\silverwing-logistics-platform\silverwing-ai-service\src\main\java\com\silverwing\ai"

$moves = @(
    @{from="controller"; to="trigger/controller"},
    @{from="dto"; to="application/dto"},
    @{from="service"; to="application"}
)

foreach ($m in $moves) {
    $fromDir = Join-Path $src $m.from
    $toDir = Join-Path $src $m.to
    if (Test-Path $fromDir) {
        Get-ChildItem -Path $fromDir -Recurse -File | ForEach-Object {
            $rel = $_.FullName.Substring($fromDir.Length + 1)
            $dest = Join-Path $toDir $rel
            $destDir = Split-Path $dest
            if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
            Move-Item -Path $_.FullName -Destination $dest -Force
        }
        Remove-Item -Path $fromDir -Recurse -Force
        Write-Host "Moved $($m.from) -> $($m.to)"
    }
}

Get-ChildItem -Path $src -Recurse -File -Filter *.java | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw -Encoding UTF8
    $content = $content -replace 'com\.silverwing\.ai\.controller', 'com.silverwing.ai.trigger.controller'
    $content = $content -replace 'com\.silverwing\.ai\.dto', 'com.silverwing.ai.application.dto'
    $content = $content -replace 'com\.silverwing\.ai\.service', 'com.silverwing.ai.application'
    Set-Content -Path $_.FullName -Value $content -Encoding UTF8 -NoNewline
}
Write-Host "Package rewrite done."
