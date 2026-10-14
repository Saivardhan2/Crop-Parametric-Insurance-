@echo off
echo ==========================================
echo    IrrigaShield Microservices Status Check
echo ===========================================
echo.
powershell -NoProfile -Command "$svcs = @{ 8761='Eureka Server'; 8080='API Gateway'; 8081='Auth Service'; 8082='Farm Service'; 8083='Policy Service'; 8084='Event Claims Service'; 4200='Angular Frontend' }; $svcs.Keys | ForEach-Object { $p=[int]$_; $tcp=New-Object System.Net.Sockets.TcpClient; try { $a=$tcp.BeginConnect('127.0.0.1',$p,$null,$null); if ($a.AsyncWaitHandle.WaitOne(400,$false) -and $tcp.Connected) { Write-Host ('[ UP ]  Port ' + $p + ' - ' + $svcs[$p]) -ForegroundColor Green } else { Write-Host ('[ DOWN ] Port ' + $p + ' - ' + $svcs[$p]) -ForegroundColor Red } } catch { Write-Host ('[ DOWN ] Port ' + $p + ' - ' + $svcs[$p]) -ForegroundColor Red } finally { $tcp.Close() } }"
echo.
exit /b 0
