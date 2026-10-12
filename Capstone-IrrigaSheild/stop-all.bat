@echo off
setlocal EnableDelayedExpansion

echo ==========================================
echo   Stopping all IrrigaShield microservices
echo ==========================================
echo.

set "PORTS=8761 8080 8081 8082 8083 8084"

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%A in ('netstat -a -n -o ^| findstr ":%%P" ^| findstr "LISTENING"') do (
        echo Stopping service on port %%P (PID: %%A)
        taskkill /F /PID %%A >nul 2>&1
    )
)

echo.
echo ==========================================
echo   All microservices stopped
echo ==========================================
echo.

pause
exit /b 0

