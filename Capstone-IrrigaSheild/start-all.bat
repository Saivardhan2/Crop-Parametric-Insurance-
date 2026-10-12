@echo off
setlocal EnableDelayedExpansion

REM Detect JAVA_HOME if not already set in environment
if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

if /I "%~1"=="__start" goto START_SERVICE

echo ==========================================
echo   Starting all microservices in parallel
echo ==========================================
echo.

set "SERVICES=eureka-server api-gateway auth-service farm-service policy-service event-claims-service"
set "LOG_DIR=%~dp0runtime-logs"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo Starting services...
echo.

for %%S in (%SERVICES%) do (
    echo Starting %%S...
    start "Runtime - %%S" /b "%ComSpec%" /c call "%~f0" __start %%S
    REM Give Eureka server a small head start to bind port 8761 before clients register
    if "%%S"=="eureka-server" (
        timeout /t 5 /nobreak >nul 2>&1
        if errorlevel 1 ping 127.0.0.1 -n 4 >nul
    )
)

echo.
echo ==========================================
echo   All services have been launched
echo ==========================================
echo.
echo Service Port Mapping:
echo   [8761] eureka-server
echo   [8080] api-gateway
echo   [8081] auth-service
echo   [8082] farm-service
echo   [8083] policy-service
echo   [8084] event-claims-service
echo.
echo Runtime logs:
echo %LOG_DIR%\
echo.

REM pause
exit /b 0


:START_SERVICE

set "SERVICE=%~2"
set "JAR="

REM Detect Java executable
set "JAVA_CMD=java"
if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
        set "JAVA_CMD=!JAVA_HOME!\bin\java.exe"
    )
) else (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

pushd "%~dp0%SERVICE%"

if not exist "target" (
    echo [ERROR] target directory not found for %SERVICE%
    popd
    exit /b 1
)

for /f "delims=" %%J in ('dir /b /a-d /o-d "target\*.jar" 2^>nul') do (
    echo %%J | findstr /i "\.original$" >nul
    if errorlevel 1 (
        if not defined JAR set "JAR=%%J"
    )
)

if not defined JAR (
    echo [ERROR] No JAR found for %SERVICE%
    echo No JAR found for %SERVICE% > "%~dp0runtime-logs\%SERVICE%.log"
    popd
    exit /b 1
)

echo [%SERVICE%] Using JAR: !JAR!
echo [%SERVICE%] Starting...

"!JAVA_CMD!" -Deureka.instance.ip-address=127.0.0.1 -Deureka.instance.prefer-ip-address=true -jar "target\!JAR!" > "%~dp0runtime-logs\%SERVICE%.log" 2>&1

popd
exit /b 0

