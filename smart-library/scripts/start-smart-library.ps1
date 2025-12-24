param(
    [int]$Port = 8082
)

Write-Host "Checking port $Port ..."
$listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    $pid = $listener.OwningProcess
    $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
    Write-Host "Port $Port is in use by PID=$pid ($($proc.ProcessName)). Stopping it..."
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
} else {
    Write-Host "Port $Port is free."
}

# Uygulamayı fork modunda başlat ki terminale yapışmasın
Write-Host "Starting application on port $Port ..."
$runArgs = "--server.port=$Port"
# spring-boot:run için admin ve temiz kapatma desteği
$env:SPRING_APPLICATION_JSON = '{"spring":{"application":{"admin":{"enabled":true}}}}'
& ..\mvnw.cmd spring-boot:run -Dspring-boot.run.fork=true -Dspring-boot.run.arguments=$runArgs
