Get-ChildItem -Path "d:\workspace\silverwing-logistics-platform\silverwing-ai-service\src" -Recurse -Filter *.java | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
        Write-Host "Stripped BOM: $($_.Name)"
    }
}
Write-Host "BOM strip done."
